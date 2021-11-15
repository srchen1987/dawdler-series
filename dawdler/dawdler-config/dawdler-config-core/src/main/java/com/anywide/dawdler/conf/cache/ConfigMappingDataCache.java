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
package com.anywide.dawdler.conf.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.conf.cache.ConfigDataCache.ConfigData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConfigMappingDataCache.java
 * @Description 缓存path指定映射类型的对象
 * @date 2021年5月29日
 * @email suxuan696@gmail.com
 */
public class ConfigMappingDataCache {
	private static Map<String, Map<Class<?>, Object>> cache = new ConcurrentHashMap<>();

	public static void removeMappingData(String path) {
		cache.remove(path);
	}

	public static <T> T getMappingDataCache(String path, Class<T> mappingClass)
			throws JsonMappingException, JsonProcessingException {
		Map<Class<?>, Object> realObjMap = cache.get(path);
		if (realObjMap == null) {
			return loadAndParse(path, mappingClass);
		} else {
			Object obj = realObjMap.get(mappingClass);
			if (obj == null)
				return loadAndParse(path, mappingClass);
			else
				return (T) obj;
		}
	}

	public static <T> T loadAndParse(String path, Class<T> mappingClass)
			throws JsonMappingException, JsonProcessingException {
		ConfigData configData = ConfigDataCache.getConfigData(path);
		String content = null;
		if (configData != null) {
			content = configData.getContent();
		}
		if (content == null)
			return null;
		Map<Class<?>, Object> realObjMap = cache.get(path);
		if (realObjMap == null) {
			realObjMap = new ConcurrentHashMap<>();
			Map<Class<?>, Object> pre = cache.putIfAbsent(path, realObjMap);
			if (pre != null)
				realObjMap = pre;
		}
		YAMLMapper yamlMapper = YAMLMapper.builder().build();
		Object obj = yamlMapper.readValue(content, mappingClass);
		realObjMap.put(mappingClass, obj);
		return (T) obj;
	}

}
