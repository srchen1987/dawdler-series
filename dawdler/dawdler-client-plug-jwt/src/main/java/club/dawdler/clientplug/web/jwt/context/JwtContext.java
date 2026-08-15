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
package club.dawdler.clientplug.web.jwt.context;

import javax.servlet.http.HttpServletRequest;

import club.dawdler.clientplug.web.jwt.claim.JwtClaims;

/**
 * @author jackson.song
 * @version V1.0
 * 当前请求的jwt上下文,提供线程级与请求级的claims访问.由DawdlerJwtFilter在鉴权通过后绑定,
 * 业务侧可通过本类获取当前登录用户的claims,请求结束后由过滤器自动清理线程级缓存.
 */
public final class JwtContext {

	public static final String REQUEST_ATTRIBUTE_NAME = JwtContext.class.getName() + ".CLAIMS";

	private static final ThreadLocal<JwtClaims> CURRENT = new ThreadLocal<>();

	private JwtContext() {
	}

	public static void set(JwtClaims claims) {
		CURRENT.set(claims);
	}

	public static JwtClaims get() {
		return CURRENT.get();
	}

	public static void clear() {
		CURRENT.remove();
	}

	public static JwtClaims get(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		Object value = request.getAttribute(REQUEST_ATTRIBUTE_NAME);
		return value instanceof JwtClaims ? (JwtClaims) value : null;
	}

	public static void bind(HttpServletRequest request, JwtClaims claims) {
		if (request != null && claims != null) {
			request.setAttribute(REQUEST_ATTRIBUTE_NAME, claims);
		}
		set(claims);
	}

}
