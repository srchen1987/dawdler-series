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
package com.anywide.dawdler.clientplug.web.validator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 正则实现验证规则的存储类
 */
public class RegexRules {
	private static final Map<String, Pattern> regexRules = new ConcurrentHashMap<>();
	private static final Map<String, String> explains = new ConcurrentHashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(RegexRules.class);

	public static Pattern getPatternRule(String key) {
		return regexRules.get(key);
	}

	public static void registerRule(String key, String regex, String explain) {
		if (regexRules.containsKey(key)) {
			logger.warn(key + " already exists!");
			return;
		}
		Pattern pattern = Pattern.compile(regex);
		regexRules.put(key, pattern);
		explains.put(key, explain + "  状态码：[ " + key + " ] \t pattern is " + regexRules.toString());
	}

	public static void registerRule(String key, Pattern pattern, String explain) {
		if (regexRules.containsKey(key)) {
			logger.warn(key + " already exists!");
			return;
		}
		regexRules.put(key, pattern);
		explains.put(key, explain + "  状态码：[ " + key + " ] \t pattern is " + regexRules.toString());
	}

}
