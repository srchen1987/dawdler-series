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
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.web.gateway.config.GatewayOptions;
import club.dawdler.web.gateway.config.RouteDefinition;
import club.dawdler.web.gateway.route.RouteResolver;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * WebSocket 反向代理处理器，实现帧双向透传。
 */
public class WebSocketProxyHandler {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketProxyHandler.class);

	private static final String WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

	private static final Set<String> HOP_BY_HOP_HEADERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
			"te", "trailers", "transfer-encoding", "upgrade", "host", "content-length",
			"sec-websocket-key", "sec-websocket-version", "sec-websocket-protocol",
			"sec-websocket-extensions", "origin")));

	private final NetClient netClient;
	private final ExecutorService bridgeExecutor;
	private final ExecutorService handshakeExecutor;
	private final GatewayOptions.WebSocketProxyConfig config;

	public WebSocketProxyHandler(Vertx vertx, GatewayOptions.WebSocketProxyConfig config) {
		this.config = config != null ? config : new GatewayOptions.WebSocketProxyConfig();
		NetClientOptions options = new NetClientOptions()
				.setConnectTimeout(this.config.getNetConnectTimeout())
				.setTrustAll(this.config.isTrustAll());
		this.netClient = vertx.createNetClient(options);
		int bridgeMax = Math.max(1, this.config.getMaxBridgeThreads());
		this.bridgeExecutor = new ThreadPoolExecutor(bridgeMax, bridgeMax, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<>(), r -> {
					Thread t = new Thread(r, "ws-proxy-bridge");
					t.setDaemon(true);
					return t;
				}, new ThreadPoolExecutor.AbortPolicy());
		int handshakeMax = Math.max(1, this.config.getMaxHandshakeThreads());
		this.handshakeExecutor = new ThreadPoolExecutor(handshakeMax, handshakeMax, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(handshakeMax), r -> {
					Thread t = new Thread(r, "ws-proxy-handshake");
					t.setDaemon(true);
					return t;
				}, new ThreadPoolExecutor.AbortPolicy());
	}

	public void handleUpgrade(HttpServletRequest request, HttpServletResponse response,
			RouteDefinition route, String pathForMatch) {
		String wsKey = request.getHeader("Sec-WebSocket-Key");
		if (wsKey == null || wsKey.isEmpty()) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing Sec-WebSocket-Key header");
			return;
		}

		String targetUri = RouteResolver.resolveTargetUri(route);
		if (targetUri == null) {
			sendError(response, 502, "No available upstream for route: " + route.getId());
			return;
		}

		String forwardPath = RouteResolver.applyFilters(route, pathForMatch);
		String query = request.getQueryString();
		if (query != null && !query.isEmpty()) {
			forwardPath = forwardPath + "?" + query;
		}
		if (forwardPath == null || forwardPath.isEmpty()) {
			forwardPath = "/";
		}

		URI uri;
		try {
			uri = new URI(targetUri);
		} catch (Exception e) {
			logger.error("Invalid target URI: {}", targetUri, e);
			sendError(response, 502, "Invalid target URI");
			return;
		}

		String host = uri.getHost();
		boolean ssl = "wss".equalsIgnoreCase(uri.getScheme());
		int port = uri.getPort() > 0 ? uri.getPort() : (ssl ? 443 : 80);
		String logPrefix = (ssl ? "wss" : "ws") + "://" + host + ":" + port + forwardPath;

		AsyncContext asyncCtx = request.startAsync(request, response);
		asyncCtx.setTimeout(config.getConnectTimeout() + config.getHandshakeTimeout() + 1000);

		final String fwsKey = wsKey;
		final String fForwardPath = forwardPath;
		final String fHost = host;
		final int fPort = port;
		final boolean fSsl = ssl;
		final String fLogPrefix = logPrefix;
		try {
			handshakeExecutor.submit(() -> doHandshakeAndUpgrade(asyncCtx, request, response, fwsKey, fSsl, fHost,
					fPort, fForwardPath, fLogPrefix));
		} catch (RejectedExecutionException e) {
			logger.warn("Handshake executor saturated, rejecting upgrade: {}", fLogPrefix);
			asyncCtx.complete();
			sendError(response, 503, "WebSocket handshake pool saturated");
		}
	}

	private void doHandshakeAndUpgrade(AsyncContext asyncCtx, HttpServletRequest request,
			HttpServletResponse response, String wsKey, boolean ssl, String host, int port, String forwardPath,
			String logPrefix) {
		try {
			HandshakeResult hs = performUpstreamHandshake(ssl, host, port, forwardPath, request, logPrefix);
			if (hs == null) {
				sendError(response, 502, "Upstream WebSocket handshake failed");
				asyncCtx.complete();
				return;
			}

			if (hs.parser.getStatusCode() != 101) {
				forwardNonUpgradeResponse(response, hs);
				asyncCtx.complete();
				return;
			}

			String accept = computeWebSocketAccept(wsKey);
			response.setStatus(101);
			response.setHeader("Upgrade", "websocket");
			response.setHeader("Connection", "Upgrade");
			response.setHeader("Sec-WebSocket-Accept", accept);

			String subprotocol = hs.parser.getHeaders().get("Sec-WebSocket-Protocol");
			if (subprotocol != null && !subprotocol.isEmpty()) {
				response.setHeader("Sec-WebSocket-Protocol", subprotocol);
			}
			String extensions = hs.parser.getHeaders().get("Sec-WebSocket-Extensions");
			if (extensions != null && !extensions.isEmpty()) {
				response.setHeader("Sec-WebSocket-Extensions", extensions);
			}

			WebSocketProxyUpgradeHandler handler = (WebSocketProxyUpgradeHandler) request
					.upgrade(WebSocketProxyUpgradeHandler.class);
			handler.initialize(hs.socket, hs.toClientQueue, bridgeExecutor, hs.upstreamClosed, logPrefix,
					config.getReadBufferSize());
			asyncCtx.complete();
		} catch (Exception e) {
			logger.error("Handshake/upgrade failed: {}", logPrefix, e);
			if (!response.isCommitted()) {
				sendError(response, 502, "Connection upgrade failed");
			}
			asyncCtx.complete();
		}
	}

	public void close() {
		netClient.close();
		handshakeExecutor.shutdownNow();
		bridgeExecutor.shutdownNow();
	}


	private HandshakeResult performUpstreamHandshake(boolean ssl, String host, int port, String forwardPath,
			HttpServletRequest request, String logPrefix) {
		final CountDownLatch connectLatch = new CountDownLatch(1);
		final AtomicReference<NetSocket> socketRef = new AtomicReference<>();
		final AtomicReference<Throwable> errorRef = new AtomicReference<>();

		netClient.connect(port, host).onComplete(ar -> {
			if (ar.failed()) {
				errorRef.set(ar.cause());
				connectLatch.countDown();
				return;
			}
			final NetSocket socket = ar.result();
			if (ssl) {
				socket.upgradeToSsl(host).onComplete(sslAr -> {
					if (sslAr.failed()) {
						errorRef.set(sslAr.cause());
						closeQuietly(socket);
						connectLatch.countDown();
						return;
					}
					socketRef.set(socket);
					connectLatch.countDown();
				});
			} else {
				socketRef.set(socket);
				connectLatch.countDown();
			}
		});

		if (!await(connectLatch, config.getConnectTimeout())) {
			logger.error("Upstream connect timeout: {}", logPrefix);
			return null;
		}
		Throwable connectErr = errorRef.get();
		if (connectErr != null) {
			logger.error("Failed to connect to upstream {}", logPrefix, connectErr);
			return null;
		}

		final NetSocket socket = socketRef.get();
		final HandshakeParser parser = new HandshakeParser(config.getMaxHeaderSize());
		final LinkedBlockingQueue<Buffer> toClientQueue = new LinkedBlockingQueue<>();
		final AtomicBoolean upstreamClosed = new AtomicBoolean(false);
		final CountDownLatch handshakeLatch = new CountDownLatch(1);
		final AtomicReference<Throwable> handshakeError = new AtomicReference<>();

		socket.handler(buffer -> {
			if (!parser.isComplete()) {
				parser.feed(buffer);
				if (parser.isComplete()) {
					Buffer remaining = parser.getRemaining();
					if (remaining != null && remaining.length() > 0) {
						toClientQueue.offer(remaining);
					}
					handshakeLatch.countDown();
				}
			} else {
				toClientQueue.offer(buffer);
			}
		});
		socket.closeHandler(v -> {
			upstreamClosed.set(true);
			handshakeError.set(new IOException("Upstream closed before/during handshake"));
			handshakeLatch.countDown();
		});
		socket.exceptionHandler(err -> {
			upstreamClosed.set(true);
			handshakeError.set(err);
			handshakeLatch.countDown();
		});

		String handshake = buildUpstreamHandshake(host, port, forwardPath, request);
		socket.write(handshake);

		if (!await(handshakeLatch, config.getHandshakeTimeout())) {
			logger.error("Upstream handshake timeout: {}", logPrefix);
			closeQuietly(socket);
			return null;
		}
		Throwable hsErr = handshakeError.get();
		if (hsErr != null) {
			logger.error("Upstream handshake error: {}", logPrefix, hsErr);
			closeQuietly(socket);
			return null;
		}
		return new HandshakeResult(socket, parser, toClientQueue, upstreamClosed);
	}

	private void forwardNonUpgradeResponse(HttpServletResponse response, HandshakeResult hs) {
		try {
			response.setStatus(hs.parser.getStatusCode());
			for (Map.Entry<String, String> entry : hs.parser.getHeaders().entrySet()) {
				String name = entry.getKey();
				if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
					response.setHeader(name, entry.getValue());
				}
			}
			OutputStream out = response.getOutputStream();
			Buffer remaining = hs.parser.getRemaining();
			if (remaining != null && remaining.length() > 0) {
				out.write(remaining.getBytes());
			}
			Buffer buffered;
			while ((buffered = hs.toClientQueue.poll()) != null) {
				out.write(buffered.getBytes());
			}
			out.flush();
		} catch (IOException e) {
			logger.error("Failed to forward upstream non-101 response", e);
		} finally {
			closeQuietly(hs.socket);
		}
	}

	private static String buildUpstreamHandshake(String host, int port, String path, HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append("GET ").append(path).append(" HTTP/1.1\r\n");
		sb.append("Host: ").append(host);
		if (port != 80 && port != 443) {
			sb.append(":").append(port);
		}
		sb.append("\r\n");
		sb.append("Upgrade: websocket\r\n");
		sb.append("Connection: Upgrade\r\n");

		appendHeader(sb, request, "Sec-WebSocket-Key");
		appendHeader(sb, request, "Sec-WebSocket-Version");
		appendHeader(sb, request, "Sec-WebSocket-Protocol");
		appendHeader(sb, request, "Sec-WebSocket-Extensions");

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			if (HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
				continue;
			}
			Enumeration<String> values = request.getHeaders(name);
			while (values.hasMoreElements()) {
				sb.append(name).append(": ").append(values.nextElement()).append("\r\n");
			}
		}

		sb.append("\r\n");
		return sb.toString();
	}

	private static void appendHeader(StringBuilder sb, HttpServletRequest request, String headerName) {
		String value = request.getHeader(headerName);
		if (value != null && !value.isEmpty()) {
			sb.append(headerName).append(": ").append(value).append("\r\n");
		}
	}

	private static String computeWebSocketAccept(String key) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update((key + WS_GUID).getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(md.digest());
		} catch (Exception e) {
			throw new RuntimeException("Failed to compute Sec-WebSocket-Accept", e);
		}
	}

	private static void sendError(HttpServletResponse response, int status, String message) {
		try {
			if (!response.isCommitted()) {
				response.sendError(status, message);
			}
		} catch (IOException e) {
			logger.error("Failed to send error response", e);
		}
	}

	private static void closeQuietly(NetSocket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception ignored) {
			}
		}
	}

	private static boolean await(CountDownLatch latch, long timeoutMs) {
		try {
			return latch.await(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private static int findHeaderEnd(Buffer buf) {
		int len = buf.length();
		if (len < 4) {
			return -1;
		}
		for (int i = 0; i <= len - 4; i++) {
			if (buf.getByte(i) == '\r' && buf.getByte(i + 1) == '\n'
					&& buf.getByte(i + 2) == '\r' && buf.getByte(i + 3) == '\n') {
				return i;
			}
		}
		return -1;
	}

	private static final class HandshakeResult {

		final NetSocket socket;
		final HandshakeParser parser;
		final LinkedBlockingQueue<Buffer> toClientQueue;
		final AtomicBoolean upstreamClosed;

		HandshakeResult(NetSocket socket, HandshakeParser parser, LinkedBlockingQueue<Buffer> toClientQueue,
				AtomicBoolean upstreamClosed) {
			this.socket = socket;
			this.parser = parser;
			this.toClientQueue = toClientQueue;
			this.upstreamClosed = upstreamClosed;
		}

	}

	/**
	 * 解析上游 WebSocket 握手的 HTTP 响应，提取状态码、响应头和头之后的剩余数据。
	 */
	private static final class HandshakeParser {

		private final Buffer accumulated = Buffer.buffer();
		private final int maxHeaderSize;
		private boolean complete = false;
		private int statusCode;
		private final Map<String, String> headers = new LinkedHashMap<>();
		private Buffer remaining;

		HandshakeParser(int maxHeaderSize) {
			this.maxHeaderSize = maxHeaderSize;
		}

		void feed(Buffer buffer) {
			if (complete) {
				return;
			}
			accumulated.appendBuffer(buffer);
			int idx = findHeaderEnd(accumulated);
			if (idx < 0) {
				if (accumulated.length() > maxHeaderSize) {
					complete = true;
					logger.error("Upstream handshake response headers exceed {} bytes", maxHeaderSize);
				}
				return;
			}
			String headerBlock = accumulated.getString(0, idx, StandardCharsets.UTF_8.name());
			remaining = accumulated.getBuffer(idx + 4, accumulated.length());
			parse(headerBlock);
			complete = true;
		}

		private void parse(String headerBlock) {
			String[] lines = headerBlock.split("\r\n");
			if (lines.length == 0) {
				return;
			}
			String[] statusParts = lines[0].split(" ", 3);
			if (statusParts.length >= 2) {
				try {
					statusCode = Integer.parseInt(statusParts[1]);
				} catch (NumberFormatException e) {
					statusCode = 0;
				}
			}
			for (int i = 1; i < lines.length; i++) {
				int colon = lines[i].indexOf(':');
				if (colon > 0) {
					String key = lines[i].substring(0, colon).trim();
					String value = lines[i].substring(colon + 1).trim();
					headers.put(key, value);
				}
			}
		}

		boolean isComplete() {
			return complete;
		}

		int getStatusCode() {
			return statusCode;
		}

		Map<String, String> getHeaders() {
			return headers;
		}

		Buffer getRemaining() {
			return remaining;
		}
	}

}
