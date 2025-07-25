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
package club.dawdler.clientplug.web.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.session.base.SessionIdGeneratorBase;
import club.dawdler.clientplug.web.session.base.StandardSessionIdGenerator;
import club.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import club.dawdler.clientplug.web.session.message.MessageOperator;
import club.dawdler.clientplug.web.session.message.RedisMessageOperator;
import club.dawdler.clientplug.web.session.store.RedisSessionStore;
import club.dawdler.clientplug.web.session.store.SessionStore;
import club.dawdler.core.serializer.SerializeDecider;
import club.dawdler.core.serializer.Serializer;
import club.dawdler.jedis.JedisPoolFactory;
import club.dawdler.util.PropertiesUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * session过滤器
 */
public class DawdlerSessionFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(DawdlerSessionFilter.class);
	private static String cookieName = "_dawdler_key";
	private static String tokenName = "token";
	private static boolean supportHead;
	private static boolean supportParam;
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
			logger.warn("use defaultIdentityConfig in dawdler-client-plug-session jar!");
		}
		InputStream inStream = null;
		if (ps == null) {
			inStream = DawdlerSessionFilter.class.getResourceAsStream("/defaultIdentityConfig.properties");
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
			supportHead = PropertiesUtil.getIfNullReturnDefaultValueBoolean("supportHead", false, ps);
			supportParam = PropertiesUtil.getIfNullReturnDefaultValueBoolean("supportParam", false, ps);
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
	private SessionStore sessionStore;
	private SessionOperator sessionOperator;
	private Pool<Jedis> jedisPool;
	private static final String SESSION_REDIS_FILE_NAME = "session-redis";
	private MessageOperator messageOperator;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			jedisPool = JedisPoolFactory.getJedisPool(SESSION_REDIS_FILE_NAME);
		} catch (Exception e) {
			logger.error("", e);
			throw new ServletException(e);
		}
		ServletContext servletContext = filterConfig.getServletContext();
		abstractDistributedSessionManager = new DistributedCaffeineSessionManager(maxInactiveInterval, maxSize, defense,
				ipMaxInactiveInterval, ipMaxSize);
		Object listener = servletContext
				.getAttribute(AbstractDistributedSessionManager.DISTRIBUTED_SESSION_HTTP_SESSION_LISTENER);
		if (listener instanceof HttpSessionListener) {
			abstractDistributedSessionManager.setHttpSessionListener((HttpSessionListener)listener);
		}
		// 默认为kroy 需要其他的可以自行修改或扩展
		Serializer serializer = SerializeDecider.decide((byte) 2);
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
					throw new ServletException(e);
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

	class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {
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
				if (supportHead) {
					token = request.getHeader(tokenName);
				} else if (supportParam) {
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
				sessionOperator.addSession(session);
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
		String forwardHeader = request.getHeader("X-Forwarded-For");
		String ip = request.getHeader("X-Real-IP");
		if ((forwardHeader != null) && (!forwardHeader.trim().equals(""))) {
			String[] forward_headers = forwardHeader.split(",");
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
