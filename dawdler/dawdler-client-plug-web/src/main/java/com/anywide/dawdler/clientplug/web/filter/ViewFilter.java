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
package com.anywide.dawdler.clientplug.web.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import com.anywide.dawdler.clientplug.web.conf.WebConfigParser;
import com.anywide.dawdler.clientplug.web.handler.AbstractUrlHandler;
import com.anywide.dawdler.clientplug.web.handler.AnnotationUrlHandler;
import com.anywide.dawdler.clientplug.web.health.HealthCheck;
import com.anywide.dawdler.clientplug.web.health.WebHealth;
import com.anywide.dawdler.clientplug.web.plugs.AbstractDisplayPlug;
import com.anywide.dawdler.clientplug.web.plugs.PlugFactory;
import com.anywide.dawdler.clientplug.web.wrapper.BodyReaderHttpServletRequestWrapper;
import com.anywide.dawdler.util.JsonProcessUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author jackson.song
 * @version V1.0
 * 请求处理过滤器
 */
public class ViewFilter implements Filter {
	private AbstractUrlHandler annotationUrlHandler;
	private WebHealth webHealth;
	private String healthUri;
	private HealthCheck healthCheck;

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
		method = method.toUpperCase();
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath() + "/";
		String uriShort = uri.substring(uri.indexOf(contextPath) + contextPath.length() - 1);
		if (isHealthCheck(uriShort, request, response)) {
			return;
		}
		String type = request.getHeader("Content-Type");
		boolean isJson = type != null && AbstractDisplayPlug.MIME_TYPE_JSON.contains(type);
		if (isJson) {
			request = new BodyReaderHttpServletRequestWrapper(request);
		}
		try {
			boolean status = annotationUrlHandler.handleUrl(uriShort, method, request, response);
			if (!status) {
				chain.doFilter(request, response);
			}
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	public void init(FilterConfig config) throws ServletException {
		ServletContext servletContext = config.getServletContext();
		annotationUrlHandler = new AnnotationUrlHandler();
		PlugFactory.initFactory(servletContext);
		healthCheck = WebConfigParser.getWebConfig().getHealthCheck();
		if (healthCheck != null && healthCheck.isCheck()) {
			healthUri = healthCheck.getUri() == null ? "/health" : healthCheck.getUri();
			webHealth = new WebHealth(servletContext.getContextPath(), healthCheck);
		}

	}

	@Override
	public void destroy() {

	}

	private boolean isHealthCheck(String uriShort, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (healthUri != null && healthUri.equals(uriShort)) {
			if (healthCheck.getUsername() != null && healthCheck.getPassword() != null) {
				String header = request.getHeader("authorization");
				if (header != null && header.startsWith("Basic ")) {
					String base64String = header.substring(header.indexOf(" ") + 1);
					String localBase64String = Base64.getEncoder()
							.encodeToString((healthCheck.getUsername() + ":" + healthCheck.getPassword()).getBytes());
					if (base64String.equals(localBase64String)) {
						printHealth(response);
						return true;
					}
				}
				response.setStatus(401);
				response.setHeader("Cache-Control", "no-store");
				response.setDateHeader("Expires", 0);
				response.setHeader("WWW-authenticate", "Basic Realm=\"need auth!\"");
				return true;
			}
			printHealth(response);
			return true;
		}
		return false;
	}

	private void printHealth(HttpServletResponse response) throws IOException {
		response.setContentType(AbstractDisplayPlug.MIME_TYPE_JSON);
		OutputStream out = response.getOutputStream();
		JsonProcessUtil.beanToJson(out, webHealth.getServiceHealth());
		out.flush();
		out.close();
	}

}
