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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.anywide.dawdler.clientplug.web.handler.AnnotationUrlHandler;
import com.anywide.dawdler.clientplug.web.plugs.PlugFactory;
import com.anywide.dawdler.clientplug.web.wrapper.BodyReaderHttpServletRequestWrapper;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ViewFilter.java
 * @Description 请求处理过滤器
 * @date 2007年4月18日
 * @email suxuan696@gmail.com
 */
public class ViewFilter implements Filter {
	private AnnotationUrlHandler annotationUrlHander;

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
		String type = request.getHeader("Content-Type");
		boolean isJson = type != null && type.contains("application/json");
		if (isJson)
			request = new BodyReaderHttpServletRequestWrapper(request);
		try {
			boolean status = annotationUrlHander.handleUrl(uriShort, method, request, response);
			if (!status)
				chain.doFilter(request, response);
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}

	public void init(FilterConfig config) throws ServletException {
		ServletContext servletContext = config.getServletContext();
		annotationUrlHander = new AnnotationUrlHandler();
		PlugFactory.initFactory(servletContext);
	}

	@Override
	public void destroy() {

	}

}
