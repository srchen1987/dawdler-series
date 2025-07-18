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
package club.dawdler.clientplug.web.validator.entity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * 控制验证器
 */
public class ControlValidator {
	private Map<String, ControlField> controlFields;
	private Map<String, Map<String, ControlField>> fieldGroups;
	private Map<MappingFieldType, Map<String, ControlField>> globalControlFieldsCache;
	private final Map<String, Map<MappingFieldType, Map<String, ControlField>>> mappings = new HashMap<>();

	public enum MappingFieldType {
		header, body, param, path
	}

	public static MappingFieldType getMappingFieldType(String type) {
		if (type == null) {
			return MappingFieldType.param;
		}
		return MappingFieldType.valueOf(type);
	}

	public Map<String, Map<MappingFieldType, Map<String, ControlField>>> getMappings() {
		return mappings;
	}

	private Map<String, ControlField> getMappingFields(MappingFieldType mappingFieldType, String uri) {
		Map<MappingFieldType, Map<String, ControlField>> mapping = mappings.get(uri);
		if (mapping != null) {
			return mapping.get(mappingFieldType);
		}
		return null;
	}

	public Map<String, ControlField> getParamFields(String uri) {
		return getMappingFields(MappingFieldType.param, uri);
	}

	public Map<String, ControlField> getHeaderFields(String uri) {
		return getMappingFields(MappingFieldType.header, uri);
	}

	public Map<String, ControlField> getBodyFields(String uri) {
		return getMappingFields(MappingFieldType.body, uri);
	}

	public Map<String, ControlField> getPathVariableFields(String uri) {
		return getMappingFields(MappingFieldType.path, uri);
	}

	public Map<String, Map<String, ControlField>> getFieldGroups() {
		return fieldGroups;
	}

	public void setFieldGroups(Map<String, Map<String, ControlField>> fieldGroups) {
		this.fieldGroups = fieldGroups;
	}

	public Map<MappingFieldType, Map<String, ControlField>> getGlobalControlFields() {
		return globalControlFieldsCache;
	}

	public void initGlobalControlFieldsCache() {
		if (globalControlFieldsCache == null) {
			globalControlFieldsCache = new LinkedHashMap<>();
		}
	}

	public void addGlobalControlFields(MappingFieldType mappingFieldType,
			Map<String, ControlField> globalControlFields) {
		Map<String, ControlField> fieldMap = globalControlFieldsCache.get(mappingFieldType);
		if (fieldMap == null) {
			globalControlFieldsCache.put(mappingFieldType, globalControlFields);
		} else {
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
