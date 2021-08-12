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

import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.validator.operators.RegexRuleOperator;
import com.anywide.dawdler.clientplug.web.validator.operators.StringRuleOperator;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RuleOperatorExecuter.java
 * @Description 验证执行者
 * @date 2007年7月21日
 * @email suxuan696@gmail.com
 */
public class RuleOperatorExecuter {
	private static final Logger logger = LoggerFactory.getLogger(RuleOperatorExecuter.class);

	public static String invokeStringRuleOperator(String ruleKey, Object value) {
		StringRuleOperator so = RuleOperatorProvider.getStringRules().get(ruleKey);
		if (so == null)
			return null;
		return so.validate(value);
	}

	public static String invokeRegexRuleOperator(String regex, Object value) {
		Set<Entry<Pattern, RegexRuleOperator>> entrys = RuleOperatorProvider.getRegexRules().entrySet();
		for (Entry<Pattern, RegexRuleOperator> en : entrys) {
			Matcher matcher = en.getKey().matcher(regex);
			if (matcher.find()) {
				return en.getValue().validate(value, matcher);
			}
		}
		logger.warn("invalid rule \t" + regex);
		return null;
	}

	public static String autoOperator(String regex, Object value) {
		StringRuleOperator so = RuleOperatorProvider.getStringRules().get(regex);
		if (so == null) {
			return invokeRegexRuleOperator(regex, value);
		} else {
			return so.validate(value);
		}

	}
}
