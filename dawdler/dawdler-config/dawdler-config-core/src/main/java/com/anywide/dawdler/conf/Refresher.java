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
package com.anywide.dawdler.conf;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.conf.annotation.FieldConfig;
import com.anywide.dawdler.conf.cache.ConfigMappingDataCache;
import com.anywide.dawdler.conf.cache.PathMappingTargetCache;
import com.anywide.dawdler.util.ClassUtil;

/**
 * @author jackson.song
 * @version V1.0
 * @Title Refresher.java
 * @Description 刷新者，配置中心有变更时会调用此类刷新相关对象
 * @date 2021年5月30日
 * @email suxuan696@gmail.com
 */
public class Refresher {
	private static Logger logger = LoggerFactory.getLogger(Refresher.class);

	private final static JexlEngine engine = new Engine();

	public static void refreshAllConfig(Object target, boolean addPathMapping) {
		Class<?> clazz = target.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			FieldConfig conf = field.getAnnotation(FieldConfig.class);
			if (conf != null) {
				refreshFieldConfig(target, field, conf);
				if (addPathMapping)
					PathMappingTargetCache.addPathMappingTarget(conf.path(), target, field);
			}
		}
	}

	public static void refreshAllConfig(Object target) {
		refreshAllConfig(target, true);
	}

	public static void refreshFieldConfig(Object target, Field field, FieldConfig config) {
		try {
			String value = config.value();
			String path = config.path();
			field.setAccessible(true);
			if (!value.equals("")) {
				Map<String, Object> mappingData = ConfigMappingDataCache.getMappingDataCache(path, HashMap.class);
				JexlContext context = new MapContext(mappingData);
				Object obj = engine.createExpression(value).evaluate(context);
				setValue(field, target, obj);
			} else {
				Class<?> typeClass = field.getType();
				Object mappingData = ConfigMappingDataCache.getMappingDataCache(path, typeClass);
				setValue(field, target, mappingData);
			}
		} catch (Exception e) {
			logger.error("", e);
		}

	}

	public static void setValue(Field field, Object target, Object value) {
		if (value == null)
			return;
		if (value.getClass().isAssignableFrom(field.getClass())) {
			try {
				field.set(target, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
			return;
		}
		Class<?> type = field.getType();
		value = ClassUtil.convert(value, type);
		if (value != null) {
			field.setAccessible(true);
			try {
				field.set(target, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}
	}
}
