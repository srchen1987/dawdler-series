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
 * @Title: CellPhoneRuleOperator.java
 * @Description: 手机号验证
 * @author: jackson.song
 * @date: 2007年07月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class CellPhoneRuleOperator extends StringRuleOperator {
	public static final String RULEKEY = "cellPhone";
//	public static final String REGEX ="(^[1][3,5,8][0-9]{9}$)|(^0[1][3,5][0-9]{9}$)";
	public static final String REGEX = "^0?(13[0-9]|15[0-9]|18[0-9]|14[0-9]|17[0-9]|19[0-9]|16[0-9])\\d{8}$";

	public static final String EXPLAIN = "手机号验证";

	public CellPhoneRuleOperator() {
		super(RULEKEY, REGEX, EXPLAIN);
	}

	@Override
	public String validate(Object value) {
		return super.validate(value, "请输入正确的手机号!");
	}

	@Override
	public String toString() {
		return EXPLAIN;
	}

}
