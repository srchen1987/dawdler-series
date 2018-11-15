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
/**
 * 
 * @Title:  ViewFilter.java   
 * @Description:    请求处理过滤器   
 * @author: jackson.song    
 * @date:   2007年04月18日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class ViewFilter implements Filter{
	private AnnotationUrlHandler annotationUrlHander;
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String method = request.getMethod();
		if(method==null){
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		method=method.toUpperCase();
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");
		String uri = request.getRequestURI().toString();
		String contextpath  = request.getContextPath()+"/"; 
		String urishort = uri.substring(uri.indexOf(contextpath)+contextpath.length()-1);
		try {
				boolean status = annotationUrlHander.handleUrl(urishort,method, request, response);
				if(!status)chain.doFilter(request, response);
		} catch (Exception e) { 
			throw new ServletException(e);
		}
	}
	public void init(FilterConfig fConfig) throws ServletException {
		ServletContext servletContext = fConfig.getServletContext();
//		String [] interceptorNames = context.getBeanNamesForType(HandlerInterceptor.class);
//		if(interceptorNames!=null){
//			for(String name:interceptorNames){
//				InterceptorProvider.addHandlerInterceptors((HandlerInterceptor)context.getBean(name));
//			}
//		}
//		xmlHander = new XmlUrlHander(servletContext, context);
		annotationUrlHander=new AnnotationUrlHandler();
		PlugFactory.initFactory(servletContext);
	}
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
}
