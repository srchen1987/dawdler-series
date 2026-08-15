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
import java.net.URI;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.web.gateway.config.RouteDefinition;
import club.dawdler.web.gateway.route.RouteResolver;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 本地转发处理器，通过 RequestDispatcher 转发到当前应用内。
 */
public class ForwardRoutingHandler {

	private static final Logger logger = LoggerFactory.getLogger(ForwardRoutingHandler.class);

	public void forward(HttpServletRequest request, HttpServletResponse response,
			RouteDefinition route, String pathForMatch) throws IOException, ServletException {
		String forwardPath = RouteResolver.applyFilters(route, pathForMatch);
		String targetPath = resolveTargetPath(route.getUri(), forwardPath);

		if (targetPath == null || targetPath.isEmpty()) {
			targetPath = "/";
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher(targetPath);
		if (dispatcher == null) {
			logger.warn("No RequestDispatcher for forward path: {} (route={})", targetPath, route.getId());
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No local handler for: " + targetPath);
			}
			return;
		}
		logger.debug("Forwarding request to local path: {} (route={})", targetPath, route.getId());
		dispatcher.forward(request, response);
	}

	private String resolveTargetPath(String uri, String forwardPath) {
		String basePath;
		try {
			URI parsed = new URI(uri);
			basePath = parsed.getPath();
		} catch (Exception e) {
			logger.warn("Failed to parse forward URI: {}, using forward path directly: {}", uri, forwardPath, e);
			return normalizePath(forwardPath);
		}
		if (basePath == null || basePath.isEmpty() || "/".equals(basePath)) {
			return normalizePath(forwardPath);
		}
		String result;
		if (basePath.endsWith("/") && forwardPath.startsWith("/")) {
			result = basePath + forwardPath.substring(1);
		} else if (!basePath.endsWith("/") && !forwardPath.startsWith("/")) {
			result = basePath + "/" + forwardPath;
		} else {
			result = basePath + forwardPath;
		}
		return normalizePath(result);
	}

	private String normalizePath(String path) {
		if (path == null || path.isEmpty()) {
			return "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}

}
