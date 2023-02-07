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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.session.base.SessionIdGeneratorBase;
import com.anywide.dawdler.clientplug.web.session.base.StandardSessionIdGenerator;
import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.anywide.dawdler.clientplug.web.session.message.MessageOperator;
import com.anywide.dawdler.clientplug.web.session.message.RedisMessageOperator;
import com.anywide.dawdler.clientplug.web.session.store.RedisSessionStore;
import com.anywide.dawdler.clientplug.web.session.store.SessionStore;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.serializer.Serializer;
import com.anywide.dawdler.redis.JedisPoolFactory;
import com.anywide.dawdler.util.PropertiesUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerSessionFilter.java
 * @Description session过滤器
 * @date 2016年6月16日
 * @email suxuan696@gmail.com
 */
public class DawdlerSessionFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(DawdlerSessionFilter.class);
	public static String cookieName = "_dawdler_key";
	public static String tokenName = "token";
	private static boolean suportHead;
	private static boolean suportParam;
	private static String domain;
	private static String path = "/";
	private static boolean secure;
	private static int maxInactiveInterval;
	private static int maxSize;
	private static boolean defense;
	private static int ipMaxInactiveInterval;
	private static int ipMaxSize;
	private static int ipLimit;
//	private static int synFlushInterval = 0;

	static {
		Properties ps = null;
		try {
			ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter("identityConfig");
		} catch (Exception e) {
			logger.warn("use default identityConfig in dawdler-session jar!");
		}
		InputStream inStream = null;
		if (ps == null) {
			inStream = DawdlerSessionFilter.class.getResourceAsStream("/identityConfig.properties");
			ps = new Properties();
			try {
				ps.load(inStream);
			} catch (IOException e) {
				logger.error("", e);
			}
		}

		try {
			String domainString = ps.getProperty("domain");
			if (domainString != null && !domainString.trim().equals("")) {
				domain = domainString;
			}
			String pathString = ps.getProperty("path");
			if (pathString != null && !pathString.trim().equals("")) {
				path = pathString;
			}
			cookieName = ps.getProperty("cookieName");

			String tokenString = ps.getProperty("tokenName");
			if (tokenString != null && !tokenString.trim().equals("")) {
				tokenName = tokenString;
			}
			suportHead = PropertiesUtil.getIfNullReturnDefaultValueBoolean("suportHead", false, ps);
			suportParam = PropertiesUtil.getIfNullReturnDefaultValueBoolean("suportParam", false, ps);
			secure = PropertiesUtil.getIfNullReturnDefaultValueBoolean("secure", false, ps);
			maxInactiveInterval = PropertiesUtil.getIfNullReturnDefaultValueInt("maxInactiveInterval", 1800, ps);
			maxSize = PropertiesUtil.getIfNullReturnDefaultValueInt("maxSize", 65525, ps);

			defense = PropertiesUtil.getIfNullReturnDefaultValueBoolean("defense", false, ps);
			ipMaxInactiveInterval = PropertiesUtil.getIfNullReturnDefaultValueInt("ipMaxInactiveInterval", 1800, ps);
			ipMaxSize = PropertiesUtil.getIfNullReturnDefaultValueInt("ipMaxSize", 65525, ps);
			ipLimit = PropertiesUtil.getIfNullReturnDefaultValueInt("ipLimit", 8, ps);

//			String synchFlushIntervalString = ps.getProperty("synchFlushInterval");
//			if (synchFlushIntervalString != null) {
//				try {
//					int temp = Integer.parseInt(synchFlushIntervalString);
//					if (temp > 0)
//						synFlushInterval = temp;
//				} catch (Exception e) {
//				}
//			}
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

	private final SessionIdGeneratorBase sessionIdGenerator = new StandardSessionIdGenerator();
	AbstractDistributedSessionManager abstractDistributedSessionManager;
	private ServletContext servletContext;
	private SessionStore sessionStore;
	private SessionOperator sessionOperator;
	private Pool<Jedis> jedisPool;
	private final static String SESSION_REDIS_FILE_NAME = "session-redis";
	private MessageOperator messageOperator;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			jedisPool = JedisPoolFactory.getJedisPool(SESSION_REDIS_FILE_NAME);
		} catch (Exception e) {
			logger.error("", e);
			throw new ServletException(e);
		}
		servletContext = filterConfig.getServletContext();
		abstractDistributedSessionManager = new DistributedCaffeineSessionManager(maxInactiveInterval, maxSize, defense,
				ipMaxInactiveInterval, ipMaxSize);
		Object listener = servletContext
				.getAttribute(AbstractDistributedSessionManager.DISTRIBUTED_SESSION_HTTPSESSION_LISTENER);
		if (listener instanceof HttpSessionListener httpSessionListener) {
			abstractDistributedSessionManager.setHttpSessionListener(httpSessionListener);
		}

		Serializer serializer = SerializeDecider.decide((byte) 2);// 默认为kroy 需要其他的可以自行扩展
		sessionStore = new RedisSessionStore(jedisPool, serializer);
		messageOperator = new RedisMessageOperator(serializer, sessionStore, abstractDistributedSessionManager,
				jedisPool);
		sessionOperator = new SessionOperator(abstractDistributedSessionManager, sessionIdGenerator, sessionStore,
				messageOperator, serializer, servletContext);
		messageOperator.listenExpireAndDelAndChange();
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
					if (defense) {
						sessionStore.saveSession(session, getOnlineIp(httpRequest), abstractDistributedSessionManager,
								defense, ipLimit, ipMaxInactiveInterval);
					} else {
						sessionStore.saveSession(session);
					}
				} catch (Exception e) {
					logger.error("", e);
				}
				session.finish();
				if (session.isValid()) {
					Cookie cookie = new Cookie(cookieName, null);
					cookie.setMaxAge(0);
					cookie.setPath("/");
					httpReponse.addCookie(cookie);
				}
			}
		}

	}

	public String getCookieValue(Cookie[] cookies, String cookiename) {
		if (cookies == null) {
			return null;
		}
		try {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(cookiename)) {
					return cookies[i].getValue();
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void destroy() {
		messageOperator.stop();
		if (abstractDistributedSessionManager != null) {
			abstractDistributedSessionManager.close();
		}
		if (jedisPool != null) {
			jedisPool.close();
		}

	}

	class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
		private final HttpServletResponse response;
		private final HttpServletRequest request;
		private DawdlerHttpSession session;

		public HttpServletRequestWrapper(HttpServletRequest request, HttpServletResponse response) {
			super(request);
			this.response = response;
			this.request = request;
		}

		@Override
		public HttpSession getSession(boolean create) {
			if (session == null) {
				String sessionKey;
				String token = null;
				if (suportHead) {
					token = request.getHeader(tokenName);
				} else if (suportParam) {
					token = request.getParameter(tokenName);
				}
				if (token != null) {
					sessionKey = token;
				} else {
					sessionKey = getCookieValue(request.getCookies(), cookieName);
				}
				if (sessionKey != null) {
					try {
						session = sessionOperator.operationSession(sessionKey, maxInactiveInterval);
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			} else {
				session.setNew(false);
			}
			if (session != null && session.isValid()) {
				abstractDistributedSessionManager.removeSession(session.getId());
				session = null;
			}
			if (session == null && create) {
				String sessionKey = sessionIdGenerator.generateSessionId();
				setCookie(cookieName, sessionKey);
				this.session = sessionOperator.createLocalSession(sessionKey, maxInactiveInterval, true);
				HttpSessionListener httpSessionListener = abstractDistributedSessionManager.getHttpSessionListener();
				if (httpSessionListener != null) {
					HttpSessionEvent httpSessionEvent = new HttpSessionEvent(session);
					httpSessionListener.sessionCreated(httpSessionEvent);
				}
			}
			return session;
		}

		@Override
		public HttpSession getSession() {
			return this.getSession(true);
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

	private static String getOnlineIp(HttpServletRequest request) {
		String forward_header = request.getHeader("X-Forwarded-For");
		String ip = request.getHeader("X-Real-IP");
		if ((forward_header != null) && (!forward_header.trim().equals(""))) {
			String[] forward_headers = forward_header.split(",");
			String[] arrayOfString1;
			int j = (arrayOfString1 = forward_headers).length;
			for (int i = 0; i < j; i++) {
				String s = arrayOfString1[i];
				if (!s.trim().equalsIgnoreCase("unknown")) {
					return s;
				}
			}
		}
		if (ip == null || "".equals(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
