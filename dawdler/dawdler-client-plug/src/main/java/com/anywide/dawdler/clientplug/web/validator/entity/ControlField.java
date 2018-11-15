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
package com.anywide.dawdler.clientplug.web.validator.entity;
/**
 * 
 * @Title:  ControlField.java   
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2007年07月21日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class ControlField {
	@Override
	public String toString() {
		return fieldName+"\t"+rules+"\t"+fieldExplain;
	}

	private String fieldName;
	private String rules;
	private String fieldExplain;
	
	public ControlField(String fieldName,String rules,String fieldExplain) {
		this.fieldName=fieldName;
		this.rules=rules;
		this.fieldExplain=fieldExplain;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getRules() {
		return rules;
	}
	public void setRules(String rules) {
		this.rules = rules;
	}

	public String getFieldExplain() {
		return fieldExplain;
	}

	public void setFieldExplain(String fieldExplain) {
		this.fieldExplain = fieldExplain;
	}
}

