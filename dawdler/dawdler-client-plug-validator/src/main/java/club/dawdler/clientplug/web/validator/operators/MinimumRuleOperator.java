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

import java.math.BigDecimal;
import java.util.regex.Matcher;

/**
 * @author jackson.song
 * @version V1.0
 * 数字类型小于判断
 */
public class MinimumRuleOperator extends RegexRuleOperator {
	public static final String RULE_KEY = "^minimum:([-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?$)";

	public MinimumRuleOperator() {
		super(RULE_KEY);
	}

	@Override
	public String validate(Object value, Matcher matcher) {
		boolean flag = true;
		BigDecimal minimum = new BigDecimal(matcher.group(1));
		String error = "不能小于数字" + minimum.toString() + "!";

		if (value == null) {
			return null;
		}

		if (value instanceof String) {
			if (isEmpty(value.toString())) {
				return null;
			}

			BigDecimal v = null;
			try {
				v = new BigDecimal((String) value);
			} catch (Exception e) {
				// 无法解析为数字，跳过验证
				return null;
			}

			if (v.compareTo(minimum) < 0) {
				return error;
			}
		}

		if (value instanceof String[]) {
			String[] values = (String[]) value;
			for (String v : values) {
				if (isEmpty(v)) {
					continue;
				}

				BigDecimal dv = null;
				try {
					dv = new BigDecimal(v);
				} catch (Exception e) {
					// 无法解析为数字，跳过该项
					continue;
				}

				if (dv.compareTo(minimum) < 0) {
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
		return "最小数值不能小于指定数字如:minimum:25或minimum:25.32或minimum:1.4E-45，支持整数或小数!";
	}
}