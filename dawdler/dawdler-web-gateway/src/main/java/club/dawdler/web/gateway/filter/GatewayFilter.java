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
package club.dawdler.web.gateway.filter;

import java.io.IOException;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.web.gateway.config.GatewayConfig;
import club.dawdler.web.gateway.config.GatewayConfigParser;
import club.dawdler.web.gateway.config.GatewayOptions;
import club.dawdler.web.gateway.config.RouteDefinition;
import club.dawdler.web.gateway.handler.ForwardRoutingHandler;
import club.dawdler.web.gateway.handler.HttpProxyHandler;
import club.dawdler.web.gateway.handler.WebSocketProxyHandler;
import club.dawdler.web.gateway.route.RouteResolver;
import club.dawdler.web.gateway.route.RouteScheme;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 网关核心 Filter，路由匹配并分发 HTTP/WebSocket/forward 请求。
 */
@WebFilter(urlPatterns = "/*", filterName = "GatewayFilter", asyncSupported = true)
public class GatewayFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(GatewayFilter.class);
	private Vertx vertx;
	private HttpProxyHandler httpProxyHandler;
	private WebSocketProxyHandler webSocketProxyHandler;
	private ForwardRoutingHandler forwardRoutingHandler;
	private GatewayOptions options;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		GatewayConfig gatewayConfig = GatewayConfigParser.getGatewayConfig();
		options = (gatewayConfig != null && gatewayConfig.getOptions() != null)
				? gatewayConfig.getOptions()
				: new GatewayOptions();
		if (options.getVertx() == null) {
			options.setVertx(new GatewayOptions.VertxConfig());
		}
		if (options.getHttpProxy() == null) {
			options.setHttpProxy(new GatewayOptions.HttpProxyConfig());
		}
		if (options.getWebSocketProxy() == null) {
			options.setWebSocketProxy(new GatewayOptions.WebSocketProxyConfig());
		}

		VertxOptions vertxOptions = new VertxOptions();
		if (options.getVertx().getEventLoopPoolSize() > 0) {
			vertxOptions.setEventLoopPoolSize(options.getVertx().getEventLoopPoolSize());
		}
		if (options.getVertx().getWorkerPoolSize() > 0) {
			vertxOptions.setWorkerPoolSize(options.getVertx().getWorkerPoolSize());
		}
		vertx = Vertx.vertx(vertxOptions);
		httpProxyHandler = new HttpProxyHandler(vertx, options.getHttpProxy());
		webSocketProxyHandler = new WebSocketProxyHandler(vertx, options.getWebSocketProxy());
		forwardRoutingHandler = new ForwardRoutingHandler();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		String method = request.getMethod();
		if (method == null) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();
		String pathForMatch = requestUri;
		if (contextPath != null && !contextPath.isEmpty()) {
			pathForMatch = requestUri.substring(contextPath.length());
		}

		RouteDefinition route = RouteResolver.matchRoute(pathForMatch);
		if (route == null) {
			chain.doFilter(request, response);
			return;
		}
		RouteScheme routeScheme = RouteScheme.fromUri(route.getUri());
		if (routeScheme != null && routeScheme.isWebSocket()) {
			if (!isWebSocketUpgradeRequest(request)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "WebSocket upgrade required");
				return;
			}
			webSocketProxyHandler.handleUpgrade(request, response, route, pathForMatch);
			return;
		}

		if (routeScheme == RouteScheme.FORWARD) {
			forwardRoutingHandler.forward(request, response, route, pathForMatch);
			return;
		}

		String targetUri = RouteResolver.resolveTargetUri(route);
		if (targetUri == null) {
			response.sendError(502, "No available upstream for route: " + route.getId());
			return;
		}

		RouteScheme scheme = RouteScheme.fromUri(targetUri);
		if (scheme == null) {
			chain.doFilter(request, response);
			return;
		}
		String forwardPath = RouteResolver.applyFilters(route, pathForMatch);
		if (scheme == RouteScheme.HTTP || scheme == RouteScheme.HTTPS
				|| scheme == RouteScheme.LB_HTTP || scheme == RouteScheme.LB_HTTPS
				|| scheme == RouteScheme.LB) {
			AsyncContext asyncCtx = request.startAsync();
			asyncCtx.setTimeout(options.getHttpProxy().getAsyncTimeout());
			httpProxyHandler.proxy(targetUri, forwardPath, request, response, asyncCtx);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		if (httpProxyHandler != null) {
			httpProxyHandler.close();
		}
		if (webSocketProxyHandler != null) {
			webSocketProxyHandler.close();
		}
		if (vertx != null) {
			vertx.close();
		}
	}

	private boolean isWebSocketUpgradeRequest(HttpServletRequest request) {
		String upgrade = request.getHeader("Upgrade");
		if (upgrade == null || !"websocket".equalsIgnoreCase(upgrade.trim())) {
			return false;
		}
		String connection = request.getHeader("Connection");
		if (connection == null || !containsToken(connection, "upgrade")) {
			logger.warn("WebSocket upgrade request missing 'Connection: upgrade' header. Connection={}", connection);
			return false;
		}
		return "GET".equalsIgnoreCase(request.getMethod());
	}

	private static boolean containsToken(String headerValue, String token) {
		if (headerValue == null) {
			return false;
		}
		String[] tokens = headerValue.split(",");
		for (String t : tokens) {
			if (token.equalsIgnoreCase(t.trim())) {
				return true;
			}
		}
		return false;
	}

}
