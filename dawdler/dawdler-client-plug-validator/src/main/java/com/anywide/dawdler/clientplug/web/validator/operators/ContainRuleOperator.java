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

import java.util.regex.Matcher;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ContainRuleOperator.java
 * @Description 包含项验证
 * @date 2007年7月22日
 * @email suxuan696@gmail.com
 */
public class ContainRuleOperator extends RegexRuleOperator {
	public ContainRuleOperator() {
		super("^contain:\\[(.+)\\]$");
	}

	@Override
	public String validate(Object value, Matcher matcher) {
		String values = matcher.group(1);
		String[] valueArray = values.split(",");
		String error = "必须包含[" + values + "]其中一项!";
		if (value == null) {
			return error;
		}
		if (value instanceof String) {
			if (!validate(valueArray, value.toString())) {
				return error;
			}
		} else if (value instanceof String[]) {
			String[] valuesArrayTemp = (String[]) value;
			for (String v : valuesArrayTemp) {
				if (!validate(valueArray, v)) {
					return error;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "规定性范围内包含验证,如：contain:[China,1] ,表单中必须出现China或1 !";
	}

	private boolean validate(String[] values, String value) {
		for (String v : values) {
			if (v.trim().equals(value.trim())) {
				return true;
			}
		}
		return false;
	}
}
