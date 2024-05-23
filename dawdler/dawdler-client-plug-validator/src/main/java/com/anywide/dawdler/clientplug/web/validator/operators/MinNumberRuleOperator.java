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
 * 数字类小于判断
 */
public class MinNumberRuleOperator extends RegexRuleOperator {
	public static final String RULE_KEY = "^minNumber:([-+]?\\d+(\\.\\d+)?$)";

	public MinNumberRuleOperator() {
		super(RULE_KEY);
	}

	@Override
	public String validate(Object value, Matcher matcher) {
		boolean flag = true;
		double i = Double.parseDouble(matcher.group(1));
		String error = "不能小于数字" + i + "!";
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
			if (v < i) {
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
				if (dv < i) {
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
		return "最小数值不能小于指定数字如:minNumber:25或minNumber:25.32，支持整数或小数!";
	}

}
