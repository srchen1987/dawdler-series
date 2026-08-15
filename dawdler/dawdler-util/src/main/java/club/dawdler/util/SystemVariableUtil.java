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
package club.dawdler.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jackson.song
 * @version V1.0
 * 系统变量操作类 系统-D传入优先与操作系统变量
 */
public class SystemVariableUtil {
	public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

	public static String resolveStringPlaceholders(String str) {
		if (str == null || !str.contains("${")) {
			return str;
		}

		Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
		StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			String placeholder = matcher.group(1);
			String defaultValue = null;

			int defaultIndex = placeholder.indexOf(':');
			if (defaultIndex > 0) {
				defaultValue = placeholder.substring(defaultIndex + 1);
				placeholder = placeholder.substring(0, defaultIndex);
			}

			String propertyValue = System.getProperty(placeholder);
			if (propertyValue == null) {
				propertyValue = System.getenv(placeholder);
			}

			if (propertyValue == null && defaultValue != null) {
				propertyValue = defaultValue;
			}

			if (propertyValue != null) {
				matcher.appendReplacement(buffer, Matcher.quoteReplacement(propertyValue));
			}
		}

		matcher.appendTail(buffer);
		return buffer.toString();
	}

}
