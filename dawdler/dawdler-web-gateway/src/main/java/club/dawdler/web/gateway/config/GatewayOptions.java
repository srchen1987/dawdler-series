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
package club.dawdler.web.gateway.config;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 网关运行时选项配置，聚合 Vert.x、HTTP 代理、WebSocket 代理参数。
 */
public class GatewayOptions {

	/** Vert.x 运行时配置. */
	private VertxConfig vertx = new VertxConfig();

	/** HTTP/HTTPS 反向代理配置. */
	private HttpProxyConfig httpProxy = new HttpProxyConfig();

	/** WebSocket/wss 反向代理配置. */
	private WebSocketProxyConfig webSocketProxy = new WebSocketProxyConfig();

	public VertxConfig getVertx() {
		return vertx;
	}

	public void setVertx(VertxConfig vertx) {
		this.vertx = vertx;
	}

	public HttpProxyConfig getHttpProxy() {
		return httpProxy;
	}

	public void setHttpProxy(HttpProxyConfig httpProxy) {
		this.httpProxy = httpProxy;
	}

	public WebSocketProxyConfig getWebSocketProxy() {
		return webSocketProxy;
	}

	public void setWebSocketProxy(WebSocketProxyConfig webSocketProxy) {
		this.webSocketProxy = webSocketProxy;
	}

	/**
	 * Vert.x 运行时线程模型配置.
	 *
	 * <p>值为 {@code 0} 时表示使用 Vert.x 默认值,不做覆盖.
	 */
	public static class VertxConfig {

		/** 事件循环线程数, {@code 0} = Vert.x 默认 (2 * CPU 核心数). */
		private int eventLoopPoolSize = 0;

		/** worker 线程池大小, {@code 0} = Vert.x 默认 (20). */
		private int workerPoolSize = 0;

		public int getEventLoopPoolSize() {
			return eventLoopPoolSize;
		}

		public void setEventLoopPoolSize(int eventLoopPoolSize) {
			this.eventLoopPoolSize = eventLoopPoolSize;
		}

		public int getWorkerPoolSize() {
			return workerPoolSize;
		}

		public void setWorkerPoolSize(int workerPoolSize) {
			this.workerPoolSize = workerPoolSize;
		}
	}

	/**
	 * HTTP/HTTPS 反向代理配置,驱动 Vert.x {@link io.vertx.core.http.HttpClientOptions} 与
	 * Servlet 异步上下文超时.
	 *
	 * <p><b>单位注意:</b>{@code connectTimeout} 为毫秒;{@code idleTimeout} 与
	 * {@code keepAliveTimeout} 为<b>秒</b>(与 Vert.x 原生单位一致).值为 {@code 0} 表示不覆盖,
	 * 使用 Vert.x 默认值.
	 */
	public static class HttpProxyConfig {

		/** Servlet 异步上下文超时 (毫秒), {@code 0} = 不超时. */
		private long asyncTimeout = 30000L;

		/** 上游连接建立超时 (毫秒), {@code 0} = Vert.x 默认. */
		private int connectTimeout = 0;

		/** 连接空闲超时 (秒), {@code 0} = 不超时. */
		private int idleTimeout = 0;

		/** 连接池最大连接数. Vert.x 原生默认仅 5, 对网关场景严重不足, 此处设为 {@code 200}
		 *  作为开箱即用的合理默认. 压测或高并发场景应按目标在飞请求数调大(如 500~1000).
		 *  设为 {@code 0} 则回退到 Vert.x 默认值(不推荐). */
		private int maxPoolSize = 200;

		/** 是否启用 HTTP Keep-Alive. */
		private boolean keepAlive = true;

		/** Keep-Alive 超时 (秒), {@code 0} = Vert.x 默认. */
		private int keepAliveTimeout = 0;

		/** HTTPS 是否信任所有证书 (跳过证书校验), 默认 false 走 JDK truststore. */
		private boolean trustAll = false;

		/** 请求体流式转发 buffer 大小 (字节). */
		private int requestBufferSize = 8192;

		public long getAsyncTimeout() {
			return asyncTimeout;
		}

		public void setAsyncTimeout(long asyncTimeout) {
			this.asyncTimeout = asyncTimeout;
		}

		public int getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public int getIdleTimeout() {
			return idleTimeout;
		}

		public void setIdleTimeout(int idleTimeout) {
			this.idleTimeout = idleTimeout;
		}

		public int getMaxPoolSize() {
			return maxPoolSize;
		}

		public void setMaxPoolSize(int maxPoolSize) {
			this.maxPoolSize = maxPoolSize;
		}

		public boolean isKeepAlive() {
			return keepAlive;
		}

		public void setKeepAlive(boolean keepAlive) {
			this.keepAlive = keepAlive;
		}

		public int getKeepAliveTimeout() {
			return keepAliveTimeout;
		}

		public void setKeepAliveTimeout(int keepAliveTimeout) {
			this.keepAliveTimeout = keepAliveTimeout;
		}

		public boolean isTrustAll() {
			return trustAll;
		}

		public void setTrustAll(boolean trustAll) {
			this.trustAll = trustAll;
		}

		public int getRequestBufferSize() {
			return requestBufferSize;
		}

		public void setRequestBufferSize(int requestBufferSize) {
			this.requestBufferSize = requestBufferSize;
		}
	}

	/**
	 * WebSocket/wss 反向代理配置.所有时间字段单位为毫秒.
	 */
	public static class WebSocketProxyConfig {

		/** 上游 TCP 连接等待超时 (毫秒). */
		private long connectTimeout = 12000L;

		/** 上游 WebSocket 握手等待超时 (毫秒). */
		private long handshakeTimeout = 15000L;

		/** NetClient 连接建立超时 (毫秒). */
		private int netConnectTimeout = 10000;

		/** 握手响应头最大字节, 超限直接判定握手失败. */
		private int maxHeaderSize = 65536;

		/** wss 是否信任所有证书 (跳过证书校验). */
		private boolean trustAll = true;

		/** 客户端 -> 上游读取 buffer 大小 (字节). */
		private int readBufferSize = 8192;

		/**
		 * WS 桥接读写线程池上限.
		 * <p>每条 WS 连接占用 2 个线程 (reader + writer), 在连接生命周期内一直持有.
		 * 默认 256 可支撑 128 条并发 WS 连接. 该线程池使用 {@link java.util.concurrent.SynchronousQueue}
		 * (无缓冲), 所有线程繁忙时立即触发拒绝策略, 关闭已升级连接 (非 503).
		 * 调大时需注意线程栈内存开销 (每线程约 512KB~1MB).
		 */
		private int maxBridgeThreads = 256;

		/**
		 * WS 握手异步执行线程池上限.
		 * <p>握手期间释放 servlet 线程, 在 worker 线程上等待上游连接与 101 响应.
		 * 每次握手最长阻塞 {@code connectTimeout + handshakeTimeout}.
		 * <p>该线程池核心数与最大数均为 {@code maxHandshakeThreads}, 并使用同等容量的
		 * {@link java.util.concurrent.LinkedBlockingQueue}, 因此实际 503 触发阈值为
		 * {@code 2 × maxHandshakeThreads} (线程数 + 队列槽位). 默认 64 即最多容纳
		 * 128 个并发握手请求, 超出返回 503.
		 */
		private int maxHandshakeThreads = 64;

		public long getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(long connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public long getHandshakeTimeout() {
			return handshakeTimeout;
		}

		public void setHandshakeTimeout(long handshakeTimeout) {
			this.handshakeTimeout = handshakeTimeout;
		}

		public int getNetConnectTimeout() {
			return netConnectTimeout;
		}

		public void setNetConnectTimeout(int netConnectTimeout) {
			this.netConnectTimeout = netConnectTimeout;
		}

		public int getMaxHeaderSize() {
			return maxHeaderSize;
		}

		public void setMaxHeaderSize(int maxHeaderSize) {
			this.maxHeaderSize = maxHeaderSize;
		}

		public boolean isTrustAll() {
			return trustAll;
		}

		public void setTrustAll(boolean trustAll) {
			this.trustAll = trustAll;
		}

		public int getReadBufferSize() {
			return readBufferSize;
		}

		public void setReadBufferSize(int readBufferSize) {
			this.readBufferSize = readBufferSize;
		}

		public int getMaxBridgeThreads() {
			return maxBridgeThreads;
		}

		public void setMaxBridgeThreads(int maxBridgeThreads) {
			this.maxBridgeThreads = maxBridgeThreads;
		}

		public int getMaxHandshakeThreads() {
			return maxHandshakeThreads;
		}

		public void setMaxHandshakeThreads(int maxHandshakeThreads) {
			this.maxHandshakeThreads = maxHandshakeThreads;
		}
	}
}
