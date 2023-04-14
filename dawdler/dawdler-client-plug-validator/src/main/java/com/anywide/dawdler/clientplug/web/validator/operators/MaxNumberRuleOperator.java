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

/**
 * @author jackson.song
 * @version V1.0
 * @Title MaxNumberRuleOperator.java
 * @Description 数字类型大于判断
 * @date 2007年7月22日
 * @email suxuan696@gmail.com
 */
public class MaxNumberRuleOperator extends RegexRuleOperator {
	public static final String RULE_KEY = "^maxNumber:([-+]?\\d+(\\.\\d+)?$)";

	public MaxNumberRuleOperator() {
		super(RULE_KEY);
	}

	@Override
	public String validate(Object value, Matcher matcher) {
		boolean flag = true;
		double i = Double.parseDouble(matcher.group(1));
		String error = "不能大于数字" + i + "!";
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			if (isEmpty(value.toString())) {
				return null;
			}
			Double v = null;
			try {
				v = Double.parseDouble((String) value);
			} catch (Exception e) {
			}
			if (v == null) {
				return null;
			}
			if (v > i) {
				return error;
			}
		}
		if (value instanceof String[]) {
			String[] values = (String[]) value;
			for (String v : values) {
				if (isEmpty(v)) {
					continue;
				}
				Double dv = null;
				try {
					dv = Double.parseDouble(v);
				} catch (Exception e) {
				}
				if (dv == null) {
					continue;
				}
				if (dv > i) {
					flag = false;
					break;
				}

			}
		}
		if (!flag) {
			return error;
		}
		return null;
	}

	@Override
	public String toString() {
		return "最大数值不能大于指定数字如:maxNumber:25或maxNumber:25.32，支持整数或小数!";
	}
}
