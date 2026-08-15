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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.jwt.algorithm.Algorithm;
import club.dawdler.clientplug.web.jwt.algorithm.AlgorithmFactory;
import club.dawdler.clientplug.web.jwt.operator.JwtOperator;
import club.dawdler.util.PropertiesUtil;
import club.dawdler.util.spring.antpath.AntPathMatcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jackson.song
 * @version V1.0
 * jwt配置持有者,单例.读取jwt.properties(支持统一配置中心),不存在则使用jar内默认配置.
 */
public class JwtConfig {

	private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);
	private static final String CONFIG_FILE_NAME = "jwt";
	private static final JwtConfig INSTANCE = new JwtConfig();

	private final JwtOperator jwtOperator;
	private final List<String> tokenSources;
	private final String headerName;
	private final String headerPrefix;
	private final String cookieName;
	private final String paramName;
	private final boolean rejectOnInvalid;
	private final List<String> excludePaths;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final int unauthorizedStatus;
	private final byte[] unauthorizedBody;

	private JwtConfig() {
		Properties ps = loadProperties();
		String algorithm = getProperty(ps, "algorithm", "HS256");
		String secret = getProperty(ps, "secret", "");
		String rsaPrivateKey = getProperty(ps, "rsaPrivateKey", "");
		String rsaPublicKey = getProperty(ps, "rsaPublicKey", "");
		Algorithm alg = AlgorithmFactory.create(algorithm, secret, rsaPrivateKey, rsaPublicKey);

		String issuer = getProperty(ps, "issuer", "");
		Set<String> audiences = splitTrimToSet(getProperty(ps, "audience", ""), ",");
		long leewaySeconds = PropertiesUtil.getIfNullReturnDefaultValueLong("leewaySeconds", 0L, ps);
		long defaultExpireSeconds = PropertiesUtil.getIfNullReturnDefaultValueLong("expireSeconds", 7200L, ps);
		this.jwtOperator = new JwtOperator(alg, issuer, audiences, leewaySeconds, defaultExpireSeconds);

		this.tokenSources = splitTrim(getProperty(ps, "tokenFrom", "header"), ",");
		this.headerName = getProperty(ps, "headerName", "Authorization");
		this.headerPrefix = getProperty(ps, "headerPrefix", "Bearer ");
		this.cookieName = getProperty(ps, "cookieName", "token");
		this.paramName = getProperty(ps, "paramName", "token");
		this.rejectOnInvalid = PropertiesUtil.getIfNullReturnDefaultValueBoolean("rejectOnInvalid", true, ps);
		this.excludePaths = splitTrim(getProperty(ps, "excludePaths", ""), ",");
		this.unauthorizedStatus = PropertiesUtil.getIfNullReturnDefaultValueInt("unauthorizedStatus", 401, ps);
		this.unauthorizedBody = getProperty(ps, "unauthorizedBody", "{\"code\":401,\"msg\":\"unauthorized\"}")
				.getBytes(StandardCharsets.UTF_8);
	}

	public static JwtConfig getInstance() {
		return INSTANCE;
	}

	public JwtOperator getJwtOperator() {
		return jwtOperator;
	}

	public boolean isRejectOnInvalid() {
		return rejectOnInvalid;
	}

	public int getUnauthorizedStatus() {
		return unauthorizedStatus;
	}

	public byte[] getUnauthorizedBody() {
		return unauthorizedBody;
	}

	public boolean isExcluded(String path) {
		if (excludePaths.isEmpty()) {
			return false;
		}
		for (String pattern : excludePaths) {
			if (pathMatcher.match(pattern, path)) {
				return true;
			}
		}
		return false;
	}

	public String extractToken(HttpServletRequest request) {
		for (String source : tokenSources) {
			String token = null;
			if ("header".equals(source)) {
				token = extractFromHeader(request);
			} else if ("cookie".equals(source)) {
				token = extractFromCookie(request);
			} else if ("param".equals(source)) {
				token = request.getParameter(paramName);
			}
			if (token != null && !token.isEmpty()) {
				return token;
			}
		}
		return null;
	}

	private String extractFromHeader(HttpServletRequest request) {
		String value = request.getHeader(headerName);
		if (value == null || value.isEmpty()) {
			return null;
		}
		if (headerPrefix != null && !headerPrefix.isEmpty() && value.startsWith(headerPrefix)) {
			return value.substring(headerPrefix.length()).trim();
		}
		return value.trim();
	}

	private String extractFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length == 0) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private static String getProperty(Properties ps, String key, String defaultValue) {
		String value = ps.getProperty(key);
		return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
	}

	private static List<String> splitTrim(String value, String separator) {
		if (value == null || value.trim().isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> result = new LinkedHashSet<>();
		for (String part : value.split(separator)) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				result.add(trimmed);
			}
		}
		return new ArrayList<>(result);
	}

	private static Set<String> splitTrimToSet(String value, String separator) {
		if (value == null || value.trim().isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> result = new HashSet<>();
		for (String part : value.split(separator)) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				result.add(trimmed);
			}
		}
		return result;
	}

	private static Properties loadProperties() {
		Properties ps = null;
		try {
			ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter(CONFIG_FILE_NAME);
		} catch (Exception e) {
			logger.warn("use defaultJwtConfig in dawdler-client-plug-jwt jar!");
		}
		if (ps == null) {
			ps = new Properties();
			try (InputStream in = JwtConfig.class.getResourceAsStream("/defaultJwtConfig.properties")) {
				if (in == null) {
					throw new IllegalStateException("defaultJwtConfig.properties not found in classpath");
				}
				ps.load(in);
			} catch (IOException e) {
				throw new IllegalStateException("load defaultJwtConfig.properties error", e);
			}
		}
		return ps;
	}

}
