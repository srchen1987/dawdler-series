/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.dawdler.web.gateway.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.PoolOptions;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.streams.WriteStream;

import club.dawdler.web.gateway.config.GatewayOptions;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 基于 Vert.x HttpClient 的 HTTP/HTTPS 反向代理处理器。
 */
public class HttpProxyHandler {

	private static final Logger logger = LoggerFactory.getLogger(HttpProxyHandler.class);

	/**
	 * RFC 7230 §6.1 逐跳头, 代理转发时必须剥离, 由本端连接自行管理.
	 * {@code Connection} 头中列举的其它自定义头名也属逐跳头, 见 {@link #parseConnectionTokens(String)}.
	 */
	private static final Set<String> HOP_BY_HOP_HEADERS;
	static {
		Set<String> set = new HashSet<>();
		Collections.addAll(set,
				"connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
				"te", "trailer", "transfer-encoding", "upgrade");
		HOP_BY_HOP_HEADERS = Collections.unmodifiableSet(set);
	}

	private final Vertx vertx;
	private final HttpClient httpClient;
	private final GatewayOptions.HttpProxyConfig config;

	public HttpProxyHandler(Vertx vertx, GatewayOptions.HttpProxyConfig config) {
		this.vertx = vertx;
		this.config = config != null ? config : new GatewayOptions.HttpProxyConfig();
		HttpClientOptions options = new HttpClientOptions();
		if (this.config.getConnectTimeout() > 0) {
			options.setConnectTimeout(this.config.getConnectTimeout());
		}
		if (this.config.getIdleTimeout() > 0) {
			options.setIdleTimeout(this.config.getIdleTimeout());
		}
		options.setKeepAlive(this.config.isKeepAlive());
		if (this.config.getKeepAliveTimeout() > 0) {
			options.setKeepAliveTimeout(this.config.getKeepAliveTimeout());
		}
		options.setTrustAll(this.config.isTrustAll());
		PoolOptions poolOptions = new PoolOptions();
		if (this.config.getMaxPoolSize() > 0) {
			poolOptions.setHttp1MaxSize(this.config.getMaxPoolSize());
		}
		this.httpClient = vertx.createHttpClient(options, poolOptions);
	}

	public void proxy(String targetUri, String requestPath, HttpServletRequest req, HttpServletResponse resp,
			AsyncContext asyncCtx) throws IOException {
		ProxyContext ctx = new ProxyContext(asyncCtx, targetUri);
		try {
			URI uri = new URI(targetUri);
			String host = uri.getHost();
			int port = uri.getPort();
			if (port <= 0) {
				port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
			}
			String targetPath = uri.getPath();
			String path = rewritePath(targetPath, req.getQueryString(), requestPath);
			boolean ssl = "https".equalsIgnoreCase(uri.getScheme());

			HttpMethod method = HttpMethod.valueOf(req.getMethod().toUpperCase());

			RequestOptions requestOptions = new RequestOptions()
					.setMethod(method)
					.setHost(host)
					.setPort(port)
					.setURI(path)
					.setSsl(ssl);

			asyncCtx.addListener(new AsyncListener() {
				@Override
				public void onTimeout(AsyncEvent event) {
					if (!ctx.completed.get()) {
						logger.warn("Async timeout, aborting upstream: {}", targetUri);
						ctx.abortUpstream("async timeout");
					}
					ctx.complete();
				}

				@Override
				public void onError(AsyncEvent event) {
					if (!ctx.completed.get()) {
						logger.warn("Async error, aborting upstream: {}", targetUri);
						ctx.abortUpstream("async error");
					}
					ctx.complete();
				}

				@Override
				public void onComplete(AsyncEvent event) {
				}

				@Override
				public void onStartAsync(AsyncEvent event) {
				}
			});

			httpClient.request(requestOptions)
					.onSuccess(clientReq -> {
						ctx.upstreamReq.set(clientReq);
						try {
							copyRequestHeaders(req, clientReq);

							clientReq.response()
									.onSuccess(clientResp -> handleResponse(clientResp, resp, ctx))
									.onFailure(err -> {
										logger.error("Proxy response failed: " + targetUri, err);
										ctx.abortUpstream("response failure");
										ctx.complete();
									});

							sendRequestBody(req, clientReq, ctx);
						} catch (Exception e) {
							logger.error("Failed to setup proxy request", e);
							ctx.abortUpstream("setup failure");
							ctx.complete();
						}
					})
					.onFailure(err -> {
						logger.error("Proxy connect failed: " + targetUri, err);
						ctx.complete();
					});

		} catch (Exception e) {
			logger.error("Proxy error", e);
			ctx.complete();
		}
	}

	private void copyRequestHeaders(HttpServletRequest req, HttpClientRequest clientReq) {
		Set<String> connectionTokens = parseConnectionTokens(req.getHeader("Connection"));
		Enumeration<String> headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			String lower = name.toLowerCase(Locale.ROOT);
			if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)
					|| HOP_BY_HOP_HEADERS.contains(lower) || connectionTokens.contains(lower)) {
				continue;
			}
			Enumeration<String> values = req.getHeaders(name);
			while (values.hasMoreElements()) {
				clientReq.putHeader(name, values.nextElement());
			}
		}
	}

	private void sendRequestBody(HttpServletRequest req, HttpClientRequest clientReq, ProxyContext ctx)
			throws IOException {
		long contentLength = req.getContentLengthLong();
		if (contentLength > 0) {
			clientReq.putHeader("Content-Length", String.valueOf(contentLength));
		}
		InputStream in = req.getInputStream();
		if (contentLength == 0) {
			closeQuietly(in);
			clientReq.end();
			return;
		}
		pipeRequest(in, clientReq, contentLength, ctx);
	}

	private void pipeRequest(InputStream in, HttpClientRequest clientReq, long contentLength, ProxyContext ctx) {
		byte[] buf = new byte[config.getRequestBufferSize()];
		readNextChunk(in, clientReq, buf, contentLength, ctx);
	}

	private void readNextChunk(InputStream in, HttpClientRequest clientReq, byte[] buf, long contentLength,
			ProxyContext ctx) {
		if (ctx.completed.get()) {
			closeQuietly(in);
			return;
		}
		vertx.<Integer>executeBlocking(() -> in.read(buf)).onComplete(ar -> {
			if (ctx.completed.get()) {
				closeQuietly(in);
				return;
			}
			if (ar.failed()) {
				logger.error("Failed to stream request body: " + ctx.targetUri, ar.cause());
				closeQuietly(in);
				ctx.abortUpstream("request body read failure");
				ctx.complete();
				return;
			}
			int len = ar.result();
			if (len == -1) {
				closeQuietly(in);
				clientReq.end().onFailure(e -> logger.error("Failed to end upstream request: " + ctx.targetUri, e));
				return;
			}
			try {
				if (contentLength < 0 && !clientReq.isChunked()) {
					clientReq.setChunked(true);
				}
				Buffer chunk = Buffer.buffer(len);
				chunk.setBytes(0, buf, 0, len);
				clientReq.write(chunk).onFailure(e -> {
					logger.error("Failed to write upstream request body: " + ctx.targetUri, e);
					closeQuietly(in);
					ctx.abortUpstream("request body write failure");
					ctx.complete();
				});
				if (ctx.completed.get()) {
					closeQuietly(in);
					return;
				}
				if (clientReq.writeQueueFull()) {
					clientReq.drainHandler(v -> readNextChunk(in, clientReq, buf, contentLength, ctx));
				} else {
					readNextChunk(in, clientReq, buf, contentLength, ctx);
				}
			} catch (Exception e) {
				logger.error("Failed to stream request body chunk: " + ctx.targetUri, e);
				closeQuietly(in);
				ctx.abortUpstream("request body chunk failure");
				ctx.complete();
			}
		});
	}

	private void handleResponse(HttpClientResponse clientResp, HttpServletResponse resp, ProxyContext ctx) {
		try {
			resp.setStatus(clientResp.statusCode());
			copyResponseHeaders(clientResp, resp);
			OutputStream out;
			try {
				out = resp.getOutputStream();
			} catch (IOException e) {
				logger.error("Failed to get output stream: " + ctx.targetUri, e);
				ctx.abortUpstream("output stream failure");
				ctx.complete();
				return;
			}
			ServletOutputWriteStream adapter = new ServletOutputWriteStream(out);
			clientResp.pipeTo(adapter).onComplete(ar -> {
				if (ar.failed()) {
					logger.error("Failed to stream response body: " + ctx.targetUri, ar.cause());
					ctx.abortUpstream("response stream failure");
				}
				ctx.complete();
			});
		} catch (Exception e) {
			logger.error("Failed to send proxy response: " + ctx.targetUri, e);
			ctx.abortUpstream("response setup failure");
			ctx.complete();
		}
	}

	private void copyResponseHeaders(HttpClientResponse clientResp, HttpServletResponse resp) {
		Set<String> connectionTokens = parseConnectionTokens(clientResp.headers().get("Connection"));
		clientResp.headers().forEach(entry -> {
			String name = entry.getKey();
			String lower = name.toLowerCase(Locale.ROOT);
			if (HOP_BY_HOP_HEADERS.contains(lower) || connectionTokens.contains(lower)) {
				return;
			}
			resp.addHeader(name, entry.getValue());
		});
	}

	private static Set<String> parseConnectionTokens(String connectionHeader) {
		if (connectionHeader == null || connectionHeader.isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> tokens = new HashSet<>();
		for (String token : connectionHeader.split(",")) {
			String trimmed = token.trim();
			if (!trimmed.isEmpty()) {
				tokens.add(trimmed.toLowerCase(Locale.ROOT));
			}
		}
		return tokens;
	}

	private String rewritePath(String targetPath, String query, String requestPath) {
		if (targetPath == null || targetPath.isEmpty()) {
			targetPath = "/";
		}
		if (targetPath.endsWith("/") && requestPath.startsWith("/")) {
			targetPath = targetPath + requestPath.substring(1);
		} else if (!targetPath.endsWith("/") && !requestPath.startsWith("/")) {
			targetPath = targetPath + "/" + requestPath;
		} else {
			targetPath = targetPath + requestPath;
		}
		if (query != null) {
			return targetPath + "?" + query;
		}
		return targetPath;
	}

	private static void closeQuietly(InputStream in) {
		try {
			in.close();
		} catch (IOException e) {
		}
	}

	/**
	 * 将 Servlet 阻塞式 {@link OutputStream} 适配为 Vert.x {@link WriteStream},
	 * 供 {@link io.vertx.core.streams.ReadStream#pipeTo} 使用以获得自动背压与正确的完成时序.
	 *
	 * <p>所有方法均在同一个事件循环上下文上调用: {@code write}/{@code writeQueueFull}/{@code drainHandler}
	 * 由 {@code pipeTo} 在事件循环上调用, {@code executeBlocking} 的完成回调也回到同一上下文, 故无需同步.
	 * 同一时刻至多一个写任务在途, 既保证写入顺序, 又对上游提供自然背压, 避免内存无限堆积.
	 */
	private final class ServletOutputWriteStream implements WriteStream<Buffer> {

		private final OutputStream out;
		private boolean writeInFlight = false;
		private boolean ended = false;
		private Handler<Void> drainHandler;
		private Handler<Throwable> exceptionHandler;

		ServletOutputWriteStream(OutputStream out) {
			this.out = out;
		}

		@Override
		public Future<Void> write(Buffer data) {
			if (ended) {
				return Future.succeededFuture();
			}
			writeInFlight = true;
			byte[] bytes = data.getBytes();
			Future<Void> writeFuture = vertx.executeBlocking(() -> {
				out.write(bytes);
				out.flush();
				return null;
			});
			writeFuture.onComplete(ar -> {
				writeInFlight = false;
				if (ar.succeeded() && !ended) {
					Handler<Void> d = drainHandler;
					if (d != null) {
						d.handle(null);
					}
				} else if (ar.failed()) {
					Handler<Throwable> h = exceptionHandler;
					if (h != null) {
						h.handle(ar.cause());
					}
				}
			});
			return writeFuture;
		}

		@Override
		public Future<Void> end() {
			ended = true;
			return vertx.executeBlocking(() -> {
				out.flush();
				return null;
			});
		}

		@Override
		public boolean writeQueueFull() {
			return writeInFlight;
		}

		@Override
		public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
			this.drainHandler = handler;
			return this;
		}

		@Override
		public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
			this.exceptionHandler = handler;
			return this;
		}

		@Override
		public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
			return this;
		}
	}

	/**
	 * 单次代理调用的共享上下文: 聚合异步完成幂等控制与上游请求的取消句柄,
	 * 避免在多个回调间传递大量参数.
	 */
	private static final class ProxyContext {

		final AsyncContext asyncCtx;
		final AtomicBoolean completed = new AtomicBoolean(false);
		final AtomicReference<HttpClientRequest> upstreamReq = new AtomicReference<>();
		final String targetUri;

		ProxyContext(AsyncContext asyncCtx, String targetUri) {
			this.asyncCtx = asyncCtx;
			this.targetUri = targetUri;
		}

		void complete() {
			if (completed.compareAndSet(false, true)) {
				try {
					asyncCtx.complete();
				} catch (Exception ignored) {
				}
			}
		}

		void abortUpstream(String reason) {
			HttpClientRequest request = upstreamReq.get();
			if (request != null) {
				try {
					request.reset();
				} catch (Exception e) {
					logger.debug("Failed to reset upstream request ({}): {}", reason, targetUri, e);
				}
			}
		}
	}

	public void close() {
		if (httpClient != null) {
			httpClient.close();
		}
	}

}
