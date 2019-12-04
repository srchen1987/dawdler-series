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
 * @Title:  NegativeNumberOperator.java   
 * @Description:    负整数验证   
 * @author: jackson.song    
 * @date:   2007年07月22日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class NegativeNumberOperator extends StringRuleOperator{
	public static final String EXPLAIN ="负整数验证";
	public static final String REGEX = "^-[1-9]{1}\\d*$";
	public static final String RULEKEY = "negativeNumber";

	public NegativeNumberOperator() {
		super(RULEKEY, REGEX, EXPLAIN);
	}

	@Override
	public String validate(Object value) {
		return super.validate(value,"请输入负整数!");
	}
	@Override
	public String toString() {
		return EXPLAIN;
	}
}

