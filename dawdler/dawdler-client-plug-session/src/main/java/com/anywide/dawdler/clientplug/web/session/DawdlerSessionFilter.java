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
package com.anywide.dawdler.clientplug.web.session;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.web.session.base.SessionIdGeneratorBase;
import com.anywide.dawdler.clientplug.web.session.base.StandardSessionIdGenerator;
import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.anywide.dawdler.clientplug.web.session.store.DistributedSessionRedisUtil;
import com.anywide.dawdler.clientplug.web.session.store.RedisSessionStore;
import com.anywide.dawdler.clientplug.web.session.store.SessionStore;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.serializer.Serializer;
import com.anywide.dawdler.util.DawdlerTool;
/**
 * 
 * @Title:  DawdlerSessionFilter.java
 * @Description:    session过滤器
 * @author: jackson.song    
 * @date:   2016年6月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
@WebFilter("/*")
public class DawdlerSessionFilter implements Filter {
	private static Logger logger = LoggerFactory.getLogger(DawdlerSessionFilter.class);
	public static String cookieName;
	public static String domain;
	public static String path;
	public static boolean secure;
	private SessionIdGeneratorBase sessionIdGenerator = new StandardSessionIdGenerator();
	AbstractDistributedSessionManager abstractDistributedSessionManager;
	private ServletContext servletContext;
	private Serializer serializer;
	private SessionStore sessionStore;
	private SessionOperator sessionOperator;

	
	static {
		String filePath = DawdlerTool.getcurrentPath() + "identityConfig.properties";
		Properties ps = new Properties();
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(filePath);
			ps.load(inStream);
			String obj = ps.getProperty("domain");
			if (obj != null && !obj.trim().equals("")) {
				domain = obj;
			}
			path = ps.getProperty("path");
			cookieName = ps.getProperty("cookieName");
			if (ps.getProperty("secure") != null) {
				secure = Boolean.parseBoolean(ps.getProperty("secure"));
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}

	}
	

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		servletContext = filterConfig.getServletContext();
		abstractDistributedSessionManager = new DistributedCaffeineSessionManager();
		serializer = SerializeDecider.decide((byte) 2);//默认为kroy 需要其他的可以自行扩展
		sessionStore = new RedisSessionStore(DistributedSessionRedisUtil.getJedisPool(), serializer); 
		sessionOperator = new SessionOperator(abstractDistributedSessionManager, sessionStore, serializer, servletContext);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpReponse = (HttpServletResponse) response;
		try {
			httpRequest = new HttpServletRequestWrapper(httpRequest, httpReponse);
			chain.doFilter(httpRequest, httpReponse);
		} finally {
			DawdlerHttpSession session = (DawdlerHttpSession) httpRequest.getSession(false);
			if (session != null) {
				try {
					sessionStore.saveSession(session);
				} catch (Exception e) {
					logger.error("",e);
				} 
					session.finish();
			}
		}

	}

	public String getCookieValue(Cookie[] cookies, String cookiename) {
		if (cookies == null)
			return null;
		try {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(cookiename)) {
					return URLDecoder.decode(cookies[i].getValue().trim(), "utf-8");
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {
		private HttpServletResponse response;
		private HttpServletRequest request;
		private DawdlerHttpSession session;

		public HttpServletRequestWrapper(HttpServletRequest request, HttpServletResponse response) {
			super(request);
			this.response = response;
			this.request = request;
		}

		@Override
		public HttpSession getSession(boolean create) {
			if (session == null) {
				String sessionkey = getCookieValue(request.getCookies(), cookieName);
				if (sessionkey != null) { 
					try {
						session = sessionOperator.operationSession(sessionkey);
					} catch (Exception e) {
						logger.error("",e);
					}
				}
			} else {
				session.setNew(false);
			}
			if(session != null && session.isValid()) {
				abstractDistributedSessionManager.removeSession(session.getId());
				session = null;
			}
			if (session == null && create) {
				String sessionkey = sessionIdGenerator.generateSessionId();
				setCookie(cookieName, sessionkey);
				sessionOperator.createLocalSession(sessionkey);
			}
			return session;
		}


		@Override
		public HttpSession getSession() {
			return this.getSession(true);
		}

		private Cookie getCookie(String name) {
			Cookie[] cookies = ((HttpServletRequest) getRequest()).getCookies();
			if (cookies != null)
				for (Cookie cookie : cookies)
					if (cookie.getName().equalsIgnoreCase(name))
						return cookie;
			return null;
		}

		private void setCookie(String name, String value) {
			Cookie cookie = new Cookie(name, value);
			cookie.setMaxAge(-1);
			cookie.setPath(path);
			if (domain != null && domain.trim().length() > 0) {
				cookie.setDomain(domain);
			}
			cookie.setHttpOnly(true);
			cookie.setSecure(secure);
			response.addCookie(cookie);
		}
	}

	@Override
	public void destroy() {

	}
}
