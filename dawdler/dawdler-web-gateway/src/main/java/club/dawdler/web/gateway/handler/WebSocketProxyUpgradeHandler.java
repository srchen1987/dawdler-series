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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * Servlet HTTP Upgrade 处理器，双向透传 WebSocket 帧。
 */
public class WebSocketProxyUpgradeHandler implements HttpUpgradeHandler {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketProxyUpgradeHandler.class);

	/** 写出线程的终止标记（引用相等判断，不会与真实数据帧冲突）。 */
	static final Buffer POISON = Buffer.buffer();

	private NetSocket upstream;
	private LinkedBlockingQueue<Buffer> toClientQueue;
	private ExecutorService executor;
	private ServletInputStream clientIn;
	private ServletOutputStream clientOut;
	private int readBufferSize = 8192;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	void initialize(NetSocket upstream, LinkedBlockingQueue<Buffer> toClientQueue, ExecutorService executor,
			AtomicBoolean upstreamClosed, String logPrefix, int readBufferSize) {
		this.upstream = upstream;
		this.toClientQueue = toClientQueue;
		this.executor = executor;
		this.readBufferSize = readBufferSize > 0 ? readBufferSize : 8192;

		upstream.closeHandler(v -> {
			logger.info("Upstream closed: {}", logPrefix);
			closeBridge();
		});
		upstream.exceptionHandler(err -> {
			logger.error("Upstream error: {}", logPrefix, err);
			closeBridge();
		});

		if (upstreamClosed != null && upstreamClosed.get()) {
			closeBridge();
		}
	}

	@Override
	public void init(WebConnection wc) {
		try {
			clientIn = wc.getInputStream();
			clientOut = wc.getOutputStream();
		} catch (IOException e) {
			logger.error("Failed to obtain upgraded connection streams", e);
			closeBridge();
			return;
		}
		if (closed.get()) {
			closeBridge();
			return;
		}
		try {
			startClientWriter();
			startClientReader();
		} catch (RejectedExecutionException e) {
			logger.warn("Bridge executor saturated, closing upgraded connection");
			closeBridge();
		}
	}

	private void startClientWriter() {
		executor.submit(() -> {
			try {
				while (true) {
					Buffer buffer = toClientQueue.take();
					if (buffer == POISON || closed.get()) {
						break;
					}
					clientOut.write(buffer.getBytes());
					clientOut.flush();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				if (!closed.get()) {
					logger.error("Failed to write to client", e);
				}
			} finally {
				closeBridge();
			}
		});
	}

	private void startClientReader() {
		executor.submit(() -> {
			byte[] buf = new byte[readBufferSize];
			try {
				int len;
				while (!closed.get() && (len = clientIn.read(buf)) != -1) {
					if (len > 0) {
						upstream.write(Buffer.buffer(len).appendBytes(buf, 0, len));
					}
				}
			} catch (IOException e) {
				if (!closed.get()) {
					logger.debug("Client read error", e);
				}
			} finally {
				closeBridge();
			}
		});
	}

	@Override
	public void destroy() {
		closeBridge();
	}

	private void closeBridge() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}
		if (toClientQueue != null) {
			toClientQueue.offer(POISON);
		}
		if (upstream != null) {
			try {
				upstream.close();
			} catch (Exception ignored) {
			}
		}
		if (clientIn != null) {
			try {
				clientIn.close();
			} catch (Exception ignored) {
			}
		}
		if (clientOut != null) {
			try {
				clientOut.close();
			} catch (Exception ignored) {
			}
		}
	}

}
