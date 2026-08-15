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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 基于模板重构请求路径的路径过滤器。
 */
public class SetPathFilter implements GatewayPathFilter {

	private static final Logger logger = LoggerFactory.getLogger(SetPathFilter.class);

	private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)\\}");

	@Override
	public String getName() {
		return "SetPath";
	}

	@Override
	public String apply(String path, String args) {
		if (path == null || path.isEmpty()) {
			return path;
		}
		if (args == null || args.isEmpty()) {
			logger.warn("SetPath requires a template path, got empty.");
			return path;
		}
		String template = args.trim();
		List<String> segments = splitSegments(path);
		Matcher matcher = PLACEHOLDER.matcher(template);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			int index = Integer.parseInt(matcher.group(1));
			String value = (index >= 1 && index <= segments.size()) ? segments.get(index - 1) : "";
			matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
		}
		matcher.appendTail(sb);
		String result = sb.toString();
		if (result.isEmpty()) {
			result = "/";
		}
		return result;
	}

	private static List<String> splitSegments(String path) {
		List<String> segments = new ArrayList<>();
		if (path == null) {
			return segments;
		}
		for (String segment : path.split("/")) {
			if (!segment.isEmpty()) {
				segments.add(segment);
			}
		}
		return segments;
	}

}
