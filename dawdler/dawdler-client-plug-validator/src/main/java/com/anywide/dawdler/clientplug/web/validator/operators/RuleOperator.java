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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @Title:  RuleOperator.java   
 * @Description:    验证规则定义抽象类   
 * @author: jackson.song    
 * @date:   2007年07月22日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public abstract class RuleOperator {
	protected  Pattern pattern;
	protected String ruleKey;
	public Pattern getPattern() {
		return pattern;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	public String getRuleKey() {
		return ruleKey;
	}
	public void setRuleKey(String ruleKey) {
		this.ruleKey = ruleKey;
	}
	public RuleOperator() {
	}
	public RuleOperator(String regex,boolean isRegex) {
		if(isRegex)
		this.pattern = Pattern.compile(regex);
		else{
			this.ruleKey=regex;
		}
	}
	public abstract String validate(Object value);
	public abstract String validate(Object value,Matcher matcher);
}

