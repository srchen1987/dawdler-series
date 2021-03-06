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

import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @Title: ValidateParser.java
 * @Description: 验证解析器
 * @author: jackson.song
 * @date: 2007年07月21日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ValidateParser {
	private static Logger logger = LoggerFactory.getLogger(ValidateParser.class);

	private static void uniqueArrRules(String validaterule, Set<String> existrule) {
		String rules[] = validaterule.split("&");
		for (String rule : rules) {
			existrule.add(rule);
		}
	}

	public static String validate(String viewname, Object value, String validaterule) {
		if (validaterule == null) {
			logger.warn(viewname + "\t rule is null!");
			return null;
		}
		Set<String> set = new LinkedHashSet<String>();
		uniqueArrRules(validaterule, set);
		for (String regex : set) {
			String error = RuleOperatorExecuter.autoOperator(regex, value);
			if (error != null)
				return (viewname + error);
		}
		return null;
	}

}
