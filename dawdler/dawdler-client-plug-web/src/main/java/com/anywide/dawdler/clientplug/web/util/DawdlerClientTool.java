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
package com.anywide.dawdler.clientplug.web.util;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jackson.song
 * @version V1.0
 * 常用工具
 */
public class DawdlerClientTool {
	public static String get_onlineip(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		String forward_header = request.getHeader("X-Forwarded-For");
		if (forward_header != null && !forward_header.trim().equals("")) {
			String[] forward_headers = forward_header.split(",");
			for (String s : forward_headers) {
				if (!s.trim().equalsIgnoreCase("unknown")) {
					return s;
				}
			}
		}
		/*
		 * if(isempty(ip) || "unknown".equalsIgnoreCase(ip)) { ip =
		 * request.getHeader("x-forwarded-for"); } if(isempty(ip) ||
		 * "unknown".equalsIgnoreCase(ip)) { ip =
		 * request.getHeader("WL-Proxy-Client-IP"); } if(isempty(ip) ||
		 * "unknown".equalsIgnoreCase(ip)) { ip = request.getRemoteAddr(); }
		 */
		if (empty(ip)) {
			return request.getRemoteAddr();
		}
		return ip;
	}

	public static boolean empty(String parameter) {
		return parameter == null || parameter.trim().equals("");
	}
}
