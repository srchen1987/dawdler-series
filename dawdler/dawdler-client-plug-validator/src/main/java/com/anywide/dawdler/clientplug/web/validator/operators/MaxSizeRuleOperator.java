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
 * 
 * @Title: MaxSizeRuleOperator.java
 * @Description: 字符个数大于判断
 * @author: jackson.song
 * @date: 2007年07月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class MaxSizeRuleOperator extends RegexRuleOperator {
	public static final String RULEKEY = "^maxsize:([1-9]{1}\\d*$)";

	public MaxSizeRuleOperator() {
		super(RULEKEY);
	}

	@Override
	public String validate(Object value, Matcher matcher) {
		boolean flag = true;
		int i = Integer.parseInt(matcher.group(1));
		String error = "不能大于" + i + "个字符!";
		if (value == null)
			return null;
		if (value instanceof String) {
			if (isEmpty(value.toString()))
				return null;
			if (((String) value).trim().length() > i)
				return error;
		}
		if (value instanceof String[]) {
			String[] values = (String[]) value;
			for (String v : values) {
				if (isEmpty(v)) {
					continue;
				}
				if (v.trim().length() > i) {
					flag = false;
					break;
				}
			}
		} else if (value instanceof List) {
			List values = (List) value;
			for (Object o : values) {
				if (isEmpty(o.toString())) {
					continue;
				}
				if (o.toString().trim().length() > i) {
					flag = false;
					break;
				}
			}
		}
		if (!flag)
			return error;
		return null;
	}

	@Override
	public String toString() {
		return "字符串或数组中的字符串的长度不能大于指定长度,如：maxsize:32!";
	}
}
