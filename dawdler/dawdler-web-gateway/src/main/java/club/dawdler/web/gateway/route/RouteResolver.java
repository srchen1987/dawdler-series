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
package club.dawdler.web.gateway.route;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.util.spring.antpath.AntPathMatcher;
import club.dawdler.web.gateway.config.FilterDefinition;
import club.dawdler.web.gateway.config.GatewayConfig;
import club.dawdler.web.gateway.config.GatewayConfigParser;
import club.dawdler.web.gateway.config.PredicateDefinition;
import club.dawdler.web.gateway.config.RouteDefinition;
import club.dawdler.web.gateway.route.filter.GatewayPathFilter;
import club.dawdler.web.gateway.route.filter.GatewayPathFilterFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 路由解析器，匹配请求路径并解析目标上游地址。
 */
public class RouteResolver {

	private static final Logger logger = LoggerFactory.getLogger(RouteResolver.class);

	private static volatile List<CachedRoute> cachedRoutes = new ArrayList<>();

	private static final AntPathMatcher PATH_MATCHER = AntPathMatcher.DEFAULT_INSTANCE;

	static {
		initCache();
	}

	public static final class CachedRoute {

		public final RouteDefinition route;
		public final List<String> pathPatterns;

		CachedRoute(RouteDefinition route, List<String> pathPatterns) {
			this.route = route;
			this.pathPatterns = pathPatterns;
		}

	}

	public static List<CachedRoute> getCachedRoutes() {
		return cachedRoutes;
	}

	public static String resolveTargetUri(RouteDefinition route) {
		RouteScheme scheme = RouteScheme.fromUri(route.getUri());
		if (scheme == null) {
			logger.warn("Unknown URI scheme for route: {}", route.getId());
			return null;
		}
		

		if (scheme.isLb()) {
			String serviceName = RouteScheme.extractServiceName(route.getUri());
			if (serviceName == null) {
				return null;
			}
			String downstreamProtocol = RouteScheme.resolveDownstreamProtocol(scheme);
			String address = resolveFromDiscovery(serviceName);
			if (address == null) {
				logger.warn("No available instance for service: {}", serviceName);
				return null;
			}
			if (downstreamProtocol != null) {
				return downstreamProtocol + "://" + address;
			}
			return "http://" + address;
		}

		return route.getUri();
	}

	private static String resolveFromDiscovery(String serviceName) {
		try {
			ServiceInstancePool pool = ServiceInstancePool.getGroup(serviceName);
			if (pool == null) {
				logger.warn("No ServiceInstancePool available, cannot resolve service: {}", serviceName);
				return null;
			}
			List<String> services = pool.getInstances();
			if (services == null || services.isEmpty()) {
				return null;
			}
			return services.get(ThreadLocalRandom.current().nextInt(services.size()));
		} catch (Exception e) {
			logger.error("Failed to resolve service: " + serviceName, e);
			return null;
		}
	}

	public static synchronized void initCache() {
		GatewayConfig config = GatewayConfigParser.getGatewayConfig();
		if (config == null || config.getRoutes() == null) {
			cachedRoutes = new ArrayList<>();
			logger.warn("GatewayConfig unavailable, route cache is empty.");
			return;
		}
		List<CachedRoute> list = new ArrayList<>(config.getRoutes().size());
		for (RouteDefinition route : config.getRoutes()) {
			List<String> patterns = collectPathPatterns(route);
			list.add(new CachedRoute(route, patterns));
		}
		cachedRoutes = list;
		logger.info("Route cache initialized with {} route(s).", list.size());
	}

	private static List<String> collectPathPatterns(RouteDefinition route) {
		List<String> patterns = new ArrayList<>();
		if (route.getPredicates() == null) {
			return patterns;
		}
		for (PredicateDefinition predicate : route.getPredicates()) {
			if (!"Path".equals(predicate.getName())) {
				continue;
			}
			String pattern = predicate.getArgs();
			if (pattern == null || pattern.isEmpty()) {
				continue;
			}
			patterns.add(pattern);
		}
		return patterns;
	}

	public static RouteDefinition matchRoute(String requestUri) {
		List<CachedRoute> routes = cachedRoutes;
		if (routes.isEmpty()) {
			return null;
		}
		for (CachedRoute cached : routes) {
			for (String pattern : cached.pathPatterns) {
				if (PATH_MATCHER.match(pattern, requestUri)) {
					return cached.route;
				}
			}
		}
		return null;
	}

	public static String applyFilters(RouteDefinition route, String requestUri) {
		if (route.getFilters() == null) {
			return requestUri;
		}
		String result = requestUri;
		for (FilterDefinition filter : route.getFilters()) {
			GatewayPathFilter pathFilter = GatewayPathFilterFactory.getFilter(filter.getName());
			if (pathFilter == null) {
				logger.warn("Unknown path filter '{}' for route {}, skipping.", filter.getName(), route.getId());
				continue;
			}
			result = pathFilter.apply(result, filter.getArgs());
		}
		return result;
	}

}
