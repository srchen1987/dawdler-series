/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.dawdler.clientplug.web.jwt;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.jwt.claim.JwtClaims;
import club.dawdler.clientplug.web.jwt.context.JwtContext;
import club.dawdler.clientplug.web.jwt.exception.JwtException;
import club.dawdler.clientplug.web.jwt.operator.JwtOperator;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author jackson.song
 * @version V1.0
 * jwt鉴权过滤器,从请求中提取token并校验,通过则绑定上下文,失败则按配置拦截或放行.
 * 通过SPI注册,fragment排在web模块之前,确保在ViewFilter之前执行.
 */
public class DawdlerJwtFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(DawdlerJwtFilter.class);

	private JwtConfig config;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		config = JwtConfig.getInstance();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		try {
			if (!config.isExcluded(pathWithinContext(request))) {
				String token = config.extractToken(request);
				JwtClaims claims = null;
				if (token != null) {
					try {
						JwtOperator operator = config.getJwtOperator();
						claims = operator.parse(token);
					} catch (JwtException e) {
						logger.debug("invalid jwt token, {}", e.getMessage());
					}
				}
				if (claims != null) {
					JwtContext.bind(request, claims);
				} else if (config.isRejectOnInvalid()) {
					writeUnauthorized(response);
					return;
				}
			}
			chain.doFilter(request, response);
		} finally {
			JwtContext.clear();
		}
	}

	private void writeUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(config.getUnauthorizedStatus());
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Cache-Control", "no-store");
		response.setDateHeader("Expires", 0);
		response.setHeader("WWW-Authenticate", "Bearer");
		byte[] body = config.getUnauthorizedBody();
		response.setContentLength(body.length);
		try (OutputStream out = response.getOutputStream()) {
			out.write(body);
			out.flush();
		}
	}

	private static String pathWithinContext(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		String path;
		if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
			path = uri.substring(contextPath.length());
		} else {
			path = uri;
		}
		if (path == null || path.isEmpty()) {
			return "/";
		}
		return path;
	}

}
