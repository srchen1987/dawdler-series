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
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @Title:  ControlValidator.java   
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2007年07月21日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class ControlValidator {
	private boolean validate;
	private Map<String,ControlField> controlFields;
	private Map<String,Map<String,ControlField>> fieldGroups;
	private Map<String,ControlField> globalControlFields;
	private Map<String,Map<String,ControlField>> mappings=new HashMap<String, Map<String,ControlField>>();
	
	public Map<String, Map<String, ControlField>> getMappings() {
		return mappings;
	}
	public Map<String, Map<String, ControlField>> getFieldGroups() {
		return fieldGroups;
	}
	public Map<String, ControlField> getGlobalControlFields() {
		return globalControlFields;
	}
	public void setGlobalControlFields(Map<String, ControlField> globalControlFields) {
		this.globalControlFields = globalControlFields;
	}
	public void setFieldGroups(Map<String, Map<String, ControlField>> fieldGroups) {
		this.fieldGroups = fieldGroups;
	}
	public Map<String, ControlField> getControlFields() {
		return controlFields;
	}
	public void setControlFields(Map<String, ControlField> controlFields) {
		this.controlFields = controlFields;
	}
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
	public boolean isValidate() {
		return validate;
	}
}

