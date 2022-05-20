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

/**
 * @author jackson.song
 * @version V1.0
 * @Title NotEmptyRuleOperator.java
 * @Description 不能为空验证
 * @date 2007年7月22日
 * @email suxuan696@gmail.com
 */
public class NotEmptyRuleOperator extends StringRuleOperator {
	public static final String RULE_KEY = "notEmpty";
	public static final String EXPLAIN = "不能为空验证";

	public NotEmptyRuleOperator() {
		super(RULE_KEY, null, EXPLAIN);

	}

	@Override
	public String validate(Object value) {
		if (value == null) {
			return "不能为空!";
		}
		boolean flag = true;
		if (value instanceof String) {
			flag = !((String) value).trim().equals("");
		} else if (value instanceof String[]) {
			String[] values = (String[]) value;
			for (String v : values) {
				if (v == null) {
					flag = false;
					break;
				}
				if (v.trim().equals("")) {
					flag = false;
					break;
				}
			}
		} else if (value instanceof List) {
			List values = (List) value;
			for (Object o : values) {
				if (o == null) {
					flag = false;
					break;
				}
				if (o.toString().trim().equals("")) {
					flag = false;
					break;
				}
			}
		}
		if (!flag) {
			return "不能为空!";
		}
		return null;
	}

	@Override
	public String toString() {
		return EXPLAIN;
	}

}
