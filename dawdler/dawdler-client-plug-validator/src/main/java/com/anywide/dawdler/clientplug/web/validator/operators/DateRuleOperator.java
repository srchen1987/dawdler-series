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

/**
 * 
 * @Title: DateRuleOperator.java
 * @Description: 日期验证
 * @author: jackson.song
 * @date: 2007年07月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class DateRuleOperator extends StringRuleOperator {

	public static final String RULEKEY = "date";
	public static final String REGEX = "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)\\s+([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";
	public static final String EXPLAIN = "日期验证";

	public DateRuleOperator() {
		super(RULEKEY, REGEX, EXPLAIN);
	}

	@Override
	public String toString() {
		return EXPLAIN;
	}

	@Override
	public String validate(Object value) {
		return super.validate(value, "请输入日期格式!");
	}

}
