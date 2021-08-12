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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ValidateParser.java
 * @Description 验证解析器
 * @date 2007年7月21日
 * @email suxuan696@gmail.com
 */
public class ValidateParser {
	private static final Logger logger = LoggerFactory.getLogger(ValidateParser.class);

	private static void uniqueArrRules(String validateRule, Set<String> existRule) {
		String[] rules = validateRule.split("&");
		Collections.addAll(existRule, rules);
	}

	public static String validate(String viewName, Object value, String validateRule) {
		if (validateRule == null) {
			logger.warn(viewName + "\t rule is null!");
			return null;
		}
		Set<String> set = new LinkedHashSet<>();
		uniqueArrRules(validateRule, set);
		for (String regex : set) {
			String error = RuleOperatorExecuter.autoOperator(regex, value);
			if (error != null)
				return (viewName + error);
		}
		return null;
	}

}
