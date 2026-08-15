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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 基于正则表达式替换请求路径的路径过滤器。
 */
public class RewritePathFilter implements GatewayPathFilter {

	private static final Logger logger = LoggerFactory.getLogger(RewritePathFilter.class);

	@Override
	public String getName() {
		return "RewritePath";
	}

	@Override
	public String apply(String path, String args) {
		if (path == null || path.isEmpty()) {
			return path;
		}
		if (args == null || args.isEmpty()) {
			logger.warn("RewritePath requires 'regex, replacement' args, got empty.");
			return path;
		}
		int comma = args.indexOf(',');
		if (comma < 0) {
			logger.warn("RewritePath args must be 'regex, replacement', got: {}", args);
			return path;
		}
		String regex = args.substring(0, comma).trim();
		String replacement = args.substring(comma + 1).trim();
		if (regex.isEmpty()) {
			return path;
		}
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(path);
			return matcher.replaceAll(replacement);
		} catch (Exception e) {
			logger.warn("RewritePath failed for path {} with args [{}]: {}", path, args, e.getMessage());
			return path;
		}
	}

}
