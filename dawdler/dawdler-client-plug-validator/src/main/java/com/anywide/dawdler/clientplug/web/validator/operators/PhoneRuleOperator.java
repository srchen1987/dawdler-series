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
 * @author jackson.song
 * @version V1.0
 * @Title PhoneRuleOperator.java
 * @Description 座机验证
 * @date 2007年7月22日
 * @email suxuan696@gmail.com
 */
public class PhoneRuleOperator extends StringRuleOperator {
	public static final String RULEKEY = "phone";
	public static final String REGEX = "(^([0][1-9]{2,3}[-])?\\d{3,8}(-\\d{1,6})?$)|(^\\([0][1-9]{2,3}\\)\\d{3,8}(\\(\\d{1,6}\\))?$)|(^\\d{3,8}$)";
	public static final String EXPLAIN = "座机验证";

	public PhoneRuleOperator() {
		super(RULEKEY, REGEX, EXPLAIN);
	}

	@Override
	public String validate(Object value) {
		return validate(value, "请输入合法的电话号码！");
	}

	@Override
	public String toString() {
		return EXPLAIN;
	}

}
