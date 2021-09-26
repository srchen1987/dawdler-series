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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.validator.operators.RegexRuleOperator;
import com.anywide.dawdler.clientplug.web.validator.operators.RuleOperator;
import com.anywide.dawdler.clientplug.web.validator.operators.StringRuleOperator;
import com.anywide.dawdler.clientplug.web.validator.scanner.RuleOperatorScanner;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RuleOperatorProvider.java
 * @Description 验证规则提供则
 * @date 2007年7月21日
 * @email suxuan696@gmail.com
 */
public class RuleOperatorProvider {
	private static final Logger logger = LoggerFactory.getLogger(RuleOperatorProvider.class);
	private static final Map<String, StringRuleOperator> stringRules = new HashMap<>();
	private static final Map<Pattern, RegexRuleOperator> regexRules = new HashMap<>();
	private static final Map<String, RegexRuleOperator> checkRegexRules = new HashMap<>();

	static {
		registerRuleOperatorScanPackage(RuleOperator.class);
	}

	public static Map<String, StringRuleOperator> getStringRules() {
		return stringRules;
	}

	public static Map<Pattern, RegexRuleOperator> getRegexRules() {
		return regexRules;
	}

	public static RegexRuleOperator getRegexRule(String regex) {
		return checkRegexRules.get(regex);
	}

	public static StringRuleOperator getStringRule(String RULE_KEY) {
		return stringRules.get(RULE_KEY);
	}

	public static void registerRuleOperator(RuleOperator ro) {
		if (ro == null)
			return;
		if (ro.getRULE_KEY() != null) {
			if (stringRules.containsKey(ro.getRULE_KEY())) {
				logger.warn(ro.getRULE_KEY() + "\talready exists in "
						+ stringRules.get(ro.getRULE_KEY()).getClass().getName() + "!");
			} else {
				stringRules.put(ro.getRULE_KEY(), (StringRuleOperator) ro);
			}
		} else if (ro.getPattern() != null) {
			if (checkRegexRules.containsKey(ro.getPattern().pattern())) {
				logger.warn(ro.getPattern().pattern() + "\talready exists in "
						+ checkRegexRules.get(ro.getPattern().pattern()).getClass().getName() + "!");
			} else {
				regexRules.put(ro.getPattern(), (RegexRuleOperator) ro);
				checkRegexRules.put(ro.getPattern().pattern(), (RegexRuleOperator) ro);
			}
		}
	}

	public static void registerRuleOperatorScanPackage(Class<?> target) {
		Set<Class<?>> classes = RuleOperatorScanner.getAppClasses(target.getPackage().getName());
		for (Class<?> c : classes) {
			if (((c.getModifiers() & 1024) != 1024) && ((c.getModifiers() & 16) != 16)
					&& ((c.getModifiers() & 512) != 512) && RuleOperator.class.isAssignableFrom(c)) {
				try {
					RuleOperator ro = (RuleOperator) c.newInstance();
					registerRuleOperator(ro);
				} catch (InstantiationException e) {
					logger.warn("", e);
				} catch (IllegalAccessException e) {
					logger.warn("", e);
				}
			}
		}
	}

	public static void help() {
		System.out.println("stringRule list\t");
		Set<Entry<String, StringRuleOperator>> entrys = stringRules.entrySet();
		for (Entry<String, StringRuleOperator> en : entrys) {
			String key = en.getKey();
			System.out.println("状态码:[ " + key + " ]\t" + stringRules.get(key));
		}
		System.out.println("regexRule list\t");
		Set<Entry<String, RegexRuleOperator>> regexEntrys = checkRegexRules.entrySet();
		for (Entry<String, RegexRuleOperator> en : regexEntrys) {
			String key = en.getKey();
			System.out.println("状态码:[ " + key + " ]\t" + checkRegexRules.get(key));
		}
	}
}
