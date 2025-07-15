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
package club.dawdler.clientplug.web.util;

import jakarta.servlet.http.Cookie;

/**
 * @author jackson.song
 * @version V1.0
 * Cookie工具
 */
public class CookieUtil {
	private static final String DOMAIN = "Domain=";
	private static final String MAX_AGE = "Max-Age=";
	private static final String PATH = "Path=";
	private static final String SECURE = "Secure";
	private static final String HTTPONLY = "HttpOnly";

	public static Cookie getCookie(String cookie) {
		if (cookie == null || cookie.trim().equals("")) {
			return null;
		}
		String[] cookies = cookie.split(";");
		String nameAndValue = cookies[0];
		String[] nameValue = nameAndValue.split("=");
		if (nameValue.length != 2) {
			return null;
		}
		String cname = nameValue[0];
		String cvalue = nameValue[1];
		Cookie c = new Cookie(cname, cvalue);
		for (int i = 1; i < cookies.length; i++) {
			String ck = cookies[i].trim();
			String[] values = splitValue(ck);
			String value = null;
			if (values != null) {
				value = values[1];
			}
			if (ck.startsWith(DOMAIN)) {
				c.setDomain(value);
			} else if (ck.startsWith(MAX_AGE)) {
				try {
					c.setMaxAge(Integer.parseInt(value));
				} catch (Exception e) {
				}
			} else if (ck.startsWith(PATH)) {
				c.setPath(value);
			} else if (ck.equals(SECURE)) {
				c.setSecure(true);
			} else if (ck.equals(HTTPONLY)) {
				c.setHttpOnly(true);
			}
		}
		return c;
	}

	public static String getCookieValue(Cookie[] cookies, String name) {
		if (cookies == null || cookies.length == 0) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private static String[] splitValue(String value) {
		String[] v = value.split("=");
		if (v.length != 2) {
			return null;
		}
		return v;
	}
}
