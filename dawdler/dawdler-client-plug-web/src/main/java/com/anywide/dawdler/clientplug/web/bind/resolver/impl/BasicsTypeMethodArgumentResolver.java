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
package com.anywide.dawdler.clientplug.web.bind.resolver.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import com.anywide.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import com.anywide.dawdler.clientplug.web.exception.ConvertException;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.util.ClassUtil;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;

/**
 * @author jackson.song
 * @version V1.0
 * 获取基础类型或Model相关参数值的决策者
 */
public class BasicsTypeMethodArgumentResolver extends AbstractMethodArgumentResolver {

	@Override
	public boolean isSupport(RequestParamFieldData requestParamFieldData) {
		Class<?> type = requestParamFieldData.getType();
		if (ClassUtil.isSimpleValueType(type) || String.class == type || String[].class == type
				|| ClassUtil.isSimpleArrayType(type) || Map.class.isAssignableFrom(type) || matchType(type)) {
			return true;
		}
		return false;

	}

	@Override
	public Object resolveArgument(RequestParamFieldData requestParamFieldData, ViewForward viewForward, String uri)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Class<?> type = requestParamFieldData.getType();
		String paramName = getParameterName(requestParamFieldData);
		if (String.class == type) {
			return viewForward.paramString(paramName);
		} else if (ClassUtil.isSimpleValueType(type)) {
			String value = viewForward.paramString(paramName);
			try {
				if (value == null && type.isPrimitive()) {
					throw new ConvertException(
							uri + ":" + paramName + " value null can't convert " + type.getName() + "!");
				}
				return ClassUtil.convert(value, type);
			} catch (Exception e) {
				throw new ConvertException(
						uri + ":" + paramName + " value " + value + " can't convert " + type.getName() + "!");
			}
		} else if (String[].class == type) {
			return viewForward.paramValues(paramName);
		} else if (ClassUtil.isSimpleArrayType(type)) {
			String[] values = viewForward.paramValues(paramName);
			Object result = null;
			try {
				result = ClassUtil.convertArray(values, type);
			} catch (Exception e) {
				throw new ConvertException(uri + ":" + paramName + " value " + Arrays.toString(values)
						+ " can't convert " + type.getName() + "!");
			}
			if (result == null && type.getComponentType().isPrimitive()) {
				throw new ConvertException(uri + ":" + paramName + " value null can't convert " + type.getName() + "!");
			}
			return result;
		} else if (Map.class.isAssignableFrom(type)) {
			return viewForward.paramMaps();
		} else {
			return setField(type, viewForward, null, uri);
		}

	}

	public boolean matchType(Class<?> type) {
		return !(type.getPackage().getName().startsWith("java.") || type.isInterface() || type.isEnum()
				|| type.isAnonymousClass() || Modifier.isAbstract(type.getModifiers()));
	}

	public Object setField(Class<?> type, ViewForward viewForward, Object instance, String uri)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if (!matchType(type) || type.isArray()) {
			return instance;
		}
		if (instance == null) {
			instance = SunReflectionFactoryInstantiator.newInstance(type);
		}
		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			if ((Modifier.isFinal(field.getModifiers())) || Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			field.setAccessible(true);
			String typeName = field.getName();
			Class<?> fieldType = field.getType();
			if (String.class == fieldType) {
				field.set(instance, viewForward.paramString(typeName));
			} else if (ClassUtil.isSimpleValueType(fieldType)) {
				String value = viewForward.paramString(typeName);
				if (value == null && type.isPrimitive()) {
					throw new ConvertException(
							uri + ":" + typeName + " value null can't convert " + type.getName() + "!");
				}
				try {
					field.set(instance, ClassUtil.convert(value, fieldType));
				} catch (Exception e) {
					throw new ConvertException(
							uri + ":" + typeName + " value " + value + " can't convert " + type.getName() + "!");
				}

			} else if (String[].class == fieldType) {
				field.set(instance, viewForward.paramValues(typeName));
			} else if (ClassUtil.isSimpleArrayType(fieldType)) {
				String[] values = viewForward.paramValues(typeName);
				Object result = null;
				try {
					result = ClassUtil.convertArray(values, fieldType);
				} catch (Exception e) {
					throw new ConvertException(uri + ":" + typeName + " value " + Arrays.toString(values)
							+ " can't convert " + type.getName() + "!");
				}
				if (result == null && type.getComponentType().isPrimitive()) {
					throw new ConvertException(
							uri + ":" + typeName + " value null can't convert " + type.getName() + "!");
				}
				field.set(instance, result);
			} else {
				field.set(instance, setField(fieldType, viewForward, null, uri));
			}
		}
		return instance;
	}

}
