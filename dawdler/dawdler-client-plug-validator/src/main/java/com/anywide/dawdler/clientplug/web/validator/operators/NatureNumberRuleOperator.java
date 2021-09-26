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
 * @Title NatureNumberRuleOperator.java
 * @Description 自然数验证
 * @date 2007年7月22日
 * @email suxuan696@gmail.com
 */
public class NatureNumberRuleOperator extends StringRuleOperator {

	public static final String RULE_KEY = "natureNumber";
	public static final String REGEX = "^[0-9]+$";
	public static final String EXPLAIN = "自然数验证";

	public NatureNumberRuleOperator() {
		super(RULE_KEY, REGEX, EXPLAIN);
	}

	@Override
	public String toString() {
		return EXPLAIN;
	}

	@Override
	public String validate(Object value) {
		return super.validate(value, "请输入自然数!");
	}

}
