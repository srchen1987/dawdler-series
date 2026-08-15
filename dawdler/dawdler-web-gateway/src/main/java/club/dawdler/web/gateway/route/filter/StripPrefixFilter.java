/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.dawdler.web.gateway.route.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 去除请求路径前 N 段前缀的路径过滤器。
 */
public class StripPrefixFilter implements GatewayPathFilter {

	private static final Logger logger = LoggerFactory.getLogger(StripPrefixFilter.class);

	@Override
	public String getName() {
		return "StripPrefix";
	}

	@Override
	public String apply(String path, String args) {
		int parts = 1;
		if (args != null && !args.isEmpty()) {
			try {
				parts = Integer.parseInt(args.trim());
			} catch (NumberFormatException e) {
				logger.warn("Invalid StripPrefix parts: {}, using default 1", args);
			}
		}
		if (parts <= 0) {
			return path == null || path.isEmpty() ? "/" : path;
		}
		return stripPathPrefix(path, parts);
	}

	private static String stripPathPrefix(String path, int parts) {
		if (path == null || path.isEmpty()) {
			return "/";
		}
		String[] segments = path.split("/");
		StringBuilder sb = new StringBuilder();
		int skipped = 0;
		for (String segment : segments) {
			if (segment.isEmpty()) {
				continue;
			}
			if (skipped < parts) {
				skipped++;
				continue;
			}
			sb.append("/").append(segment);
		}
		if (sb.length() == 0) {
			sb.append("/");
		}
		return sb.toString();
	}

}
