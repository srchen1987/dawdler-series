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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ControlValidator.java
 * @Description 控制验证器
 * @date 2007年7月21日
 * @email suxuan696@gmail.com
 */
public class ControlValidator {
	private Map<String, ControlField> controlFields;
	private Map<String, Map<String, ControlField>> fieldGroups;
	private Map<MappingFeildType, Map<String, ControlField>> globalControlFieldsCache;
	private final Map<String, Map<MappingFeildType, Map<String, ControlField>>> mappings = new HashMap<>();
	public static enum MappingFeildType{
		header,
		body,
		param,
		path
	}
	
	public static MappingFeildType getMappingFeildType(String type) {
		if(type == null) {
			return MappingFeildType.param;
		}
		return MappingFeildType.valueOf(type);
	}
	public Map<String, Map<MappingFeildType, Map<String, ControlField>>> getMappings() {
		return mappings;
	}
	
	private Map<String, ControlField> getMappingFields(MappingFeildType mappingFeildType, String uri) {
		Map<MappingFeildType, Map<String, ControlField>> mapping = mappings.get(uri);
		if(mapping != null) {
			return mapping.get(mappingFeildType);
		}
		return null;
	}
	
	public Map<String, ControlField> getParamFields(String uri) {
		return getMappingFields(MappingFeildType.param, uri);
	}
	
	public Map<String, ControlField> getHeaderFields(String uri) {
		return getMappingFields(MappingFeildType.header, uri);
	}
	
	public Map<String, ControlField> getBodyFields(String uri) {
		return getMappingFields(MappingFeildType.body, uri);
	}
	
	public Map<String, ControlField> getPathVariableFields(String uri) {
		return getMappingFields(MappingFeildType.path, uri);
	}
	
	public Map<String, Map<String, ControlField>> getFieldGroups() {
		return fieldGroups;
	}

	public void setFieldGroups(Map<String, Map<String, ControlField>> fieldGroups) {
		this.fieldGroups = fieldGroups;
	}

	public Map<MappingFeildType, Map<String, ControlField>> getGlobalControlFields() {
		return globalControlFieldsCache;
	}
	
	public Map<String, ControlField> getParamGlobalFields() {
		if(globalControlFieldsCache == null) {
			return null;
		}
		return globalControlFieldsCache.get(MappingFeildType.param);
	}
	
	public void initGlobalControlFieldsCache() {
		if(globalControlFieldsCache == null) {
			globalControlFieldsCache = new LinkedHashMap<>();
		}
	}

	public void addGlobalControlFields(MappingFeildType mappingFeildType, Map<String, ControlField> globalControlFields) {
		Map<String, ControlField> fieldMap = globalControlFieldsCache.get(mappingFeildType);
		if(fieldMap == null) {
			globalControlFieldsCache.put(mappingFeildType, globalControlFields);
		}else {
			fieldMap.putAll(globalControlFields);
		}
	}

	public Map<String, ControlField> getControlFields() {
		return controlFields;
	}

	public void setControlFields(Map<String, ControlField> controlFields) {
		this.controlFields = controlFields;
	}

}
