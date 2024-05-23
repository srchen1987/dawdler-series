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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.anywide.dawdler.conf.Refresher;
import com.anywide.dawdler.conf.annotation.FieldConfig;

/**
 * @author jackson.song
 * @version V1.0
 * 缓存path指定映射类型的对象
 */
public class PathMappingTargetCache {

	private static Map<String, Map<Object, Set<Field>>> cache = new ConcurrentHashMap<>(32);

	public static void addPathMappingTarget(String path, Object target, Field field) {
		Map<Object, Set<Field>> fieldsMap = cache.get(path);
		if (fieldsMap == null) {
			fieldsMap = new ConcurrentHashMap<>(16);
			Map<Object, Set<Field>> pre = cache.putIfAbsent(path, fieldsMap);
			if (pre != null) {
				fieldsMap = pre;
			}
			bindField(fieldsMap, target, field);
		} else {
			bindField(fieldsMap, target, field);
		}
	}

	private static void bindField(Map<Object, Set<Field>> fieldsMap, Object target, Field field) {
		Set<Field> fields = fieldsMap.get(target);
		if (fields == null) {
			fields = new CopyOnWriteArraySet<>();
			Set<Field> preFields = fieldsMap.putIfAbsent(target, fields);
			if (preFields != null) {
				fields = preFields;
			}
		}
		fields.add(field);
	}

	public static void rebindAllByPath(String path) {
		Map<Object, Set<Field>> pathMappingTargetMaps = cache.get(path);
		if (pathMappingTargetMaps != null) {
			pathMappingTargetMaps.forEach((k, v) -> {
				v.forEach(field -> {
					Refresher.refreshFieldConfig(k, field, field.getAnnotation(FieldConfig.class));
				});
			});
		}
	}

	public static Map<Object, Set<Field>> getPathMappingTargetMaps(String path) {
		return cache.get(path);
	}

	public static void removeMappingByTarget(Object target) {
		cache.values().forEach(v -> {
			v.remove(target);
		});
	}

	public static void removeMappingByTargetClass(Class<?> clazz) {
		cache.values().forEach(v -> {
			Set<Object> keys = v.keySet();
			for (Object key : keys) {
				if (key.getClass() == clazz) {
					v.remove(key);
				}
			}
		});
	}
}
