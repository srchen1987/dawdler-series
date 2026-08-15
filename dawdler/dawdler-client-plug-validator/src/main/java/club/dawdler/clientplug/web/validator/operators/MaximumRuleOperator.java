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
package club.dawdler.clientplug.web.validator.operators;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.regex.Matcher;

/**
 * @author jackson.song
 * @version V1.0
 * 数字类型大于判断
 */
public class MaximumRuleOperator extends RegexRuleOperator {
	public static final String RULE_KEY = "^maximum:([-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?$)";

	public MaximumRuleOperator() {
		super(RULE_KEY);
	}

	@Override
	public String validate(Object value, Matcher matcher) {
		BigDecimal maximum = new BigDecimal(matcher.group(1));
		String error = "不能大于数字" + maximum.toString() + "!";

		if (value == null) {
			return null;
		}

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++) {
				Object element = Array.get(value, i);
				if (element == null) {
					continue;
				}
				String str = element.toString();
				if (isEmpty(str)) {
					continue;
				}
				try {
					BigDecimal dv = new BigDecimal(str);
					if (dv.compareTo(maximum) > 0) {
						return error;
					}
				} catch (Exception e) {
					continue;
				}
			}
		} else {
			String str = value.toString();
			if (isEmpty(str)) {
				return null;
			}
			try {
				BigDecimal v = new BigDecimal(str);
				if (v.compareTo(maximum) > 0) {
					return error;
				}
			} catch (Exception e) {
				return null;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return "最大数值不能大于指定数字如:maximum:25或maximum:25.32或maximum:3.4028235E38，支持整数或小数!";
	}
}