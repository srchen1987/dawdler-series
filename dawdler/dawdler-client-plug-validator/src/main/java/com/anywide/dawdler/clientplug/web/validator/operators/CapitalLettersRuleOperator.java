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
 * @Title CapitalLettersRuleOperator.java
 * @Description TODO
 * @date 2007年7月21日
 * @email suxuan696@gmail.com
 */
public class CapitalLettersRuleOperator extends StringRuleOperator {
	public static final String RULEKEY = "capitalLetters";
	public static final String REGEX = "^[A-Z]+$";
	public static final String EXPLAIN = "大写字母验证";

	public CapitalLettersRuleOperator() {
		super(RULEKEY, REGEX, EXPLAIN);
	}

	@Override
	public String toString() {
		return EXPLAIN;
	}

	@Override
	public String validate(Object value) {
		return super.validate(value, "请输入大写英文字母!");
	}

}
