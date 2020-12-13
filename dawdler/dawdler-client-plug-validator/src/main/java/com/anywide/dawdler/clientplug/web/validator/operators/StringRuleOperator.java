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
package com.anywide.dawdler.clientplug.web.validator.operators;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.anywide.dawdler.clientplug.web.validator.AbstractValidator;
import com.anywide.dawdler.clientplug.web.validator.RegexRules;
import com.anywide.dawdler.clientplug.web.validator.ValidatorFactory;

/**
 * 
 * @Title: StringRuleOperator.java
 * @Description: 普通字符串类定义抽象类
 * @author: jackson.song
 * @date: 2007年07月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public abstract class StringRuleOperator extends RuleOperator {
	public StringRuleOperator(String RULEKEY, String REGEX, String EXPLAIN) {
		super(RULEKEY, false);
		if (REGEX != null)
			RegexRules.registerRule(RULEKEY, Pattern.compile(REGEX), EXPLAIN);
	}

	@Override
	public String validate(Object value, Matcher matcher) {
		return null;
	}

	protected String validate(Object value, String errorMessage) {
		if (value == null)
			return null;
		AbstractValidator validator = ValidatorFactory.getValidator(ruleKey);
		if (validator == null)
			return null;
		boolean flag = true;
		if (value instanceof String) {
			flag = validator.validate(value.toString());
		} else if (value instanceof String[]) {
			String[] values = (String[]) value;
			for (String v : values) {
				if (v != null)
					if (!validator.validate(v)) {
						flag = false;
						break;
					}
			}
		} else if (value instanceof List) {
			List values = (List) value;
			for (Object o : values) {
				if (o != null)
					if (!validator.validate(o.toString())) {
						flag = false;
						break;
					}
			}
		}
		if (!flag) {
			return errorMessage;
		}
		return null;
	}

	@Override
	public abstract String toString();
}
