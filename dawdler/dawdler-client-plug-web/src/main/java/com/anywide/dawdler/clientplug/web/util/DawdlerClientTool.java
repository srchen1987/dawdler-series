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

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author jackson.song
 * @version V1.0
 * 常用工具
 */
public class DawdlerClientTool {

	public static String getOnlineIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		String forwardHeader = request.getHeader("X-Forwarded-For");
		if (forwardHeader != null && !forwardHeader.trim().equals("")) {
			String[] forwardHeaders = forwardHeader.split(",");	
			for (String s : forwardHeaders) {
				if (!s.trim().equalsIgnoreCase("unknown")) {
					return s;
				}
			}
		}
		if (empty(ip)) {
			return request.getRemoteAddr();
		}
		return ip;
	}


	public static boolean empty(String parameter) {
		return parameter == null || parameter.trim().equals("");
	}
}
