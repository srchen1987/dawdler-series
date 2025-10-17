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
package club.dawdler.clientplug.web.bind.resolver.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import club.dawdler.clientplug.web.annotation.DateTimeFormat;
import club.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import club.dawdler.clientplug.web.exception.ConvertException;
import club.dawdler.clientplug.web.handler.ViewForward;
import club.dawdler.util.ClassUtil;
import club.dawdler.util.DateUtil;
import club.dawdler.util.SunReflectionFactoryInstantiator;

/**
 * @author jackson.song
 * @version V1.0
 *          获取基础类型、日期、Model相关参数值的决策者
 */
public class BasicsTypeMethodArgumentResolver extends AbstractMethodArgumentResolver {

	@Override
	public boolean isSupport(RequestParamFieldData requestParamFieldData) {
		Class<?> type = requestParamFieldData.getType();
		if (ClassUtil.isSimpleValueType(type) || String.class == type || String[].class == type
				|| ClassUtil.isSimpleArrayType(type) || Map.class.isAssignableFrom(type) || type.isEnum()
				|| (type.isArray() && type.getComponentType().isEnum())
				|| DateUtil.isDateType(type) || DateUtil.isDateTypeArray(type) || matchType(type)) {
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
		String pattern = requestParamFieldData.getPattern();
		Object date = null;
		if (type == String.class || ClassUtil.isSimpleValueType(type)) {
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
		} else if ((date = DateUtil.convertToDate(viewForward.paramString(paramName), pattern, type)) != null) {
			return date;
		} else if (DateUtil.isDateTypeArray(type)) {
			return DateUtil.convertToDateArray(viewForward.paramValues(paramName), pattern, type.getComponentType());
		} else if (type.isArray() && type.getComponentType().isEnum()) {
			String[] values = viewForward.paramValues(paramName);
			if (values == null) {
				return null;
			}
			try {
				return ClassUtil.createEnumArray((Class<Enum>) type.getComponentType(), values);
			} catch (Exception e) {
				throw new ConvertException(
						uri + ":" + paramName + " " + e.getMessage());
			}
		} else if (type.isEnum()) {
			String value = viewForward.paramString(paramName);
			if (value == null) {
				return null;
			}
			try {
				return Enum.valueOf((Class<Enum>) type, value);
			} catch (Exception e) {
				throw new ConvertException(
						uri + ":" + paramName + " " + e.getMessage());
			}
		} else {
			return setField(type, viewForward, null, uri);
		}

	}

	public boolean matchType(Class<?> type) {
		if (type.isArray()) {
			type = type.getComponentType();
		}
		return !(type.getPackage().getName().startsWith("java.") || type.isInterface()
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

		do {
			Field[] fields = type.getDeclaredFields();
			setField(fields, type, viewForward, instance, uri);
			type = type.getSuperclass();
		} while (type != null && type != Object.class);

		return instance;
	}

	public void setField(Field[] fields, Class<?> type, ViewForward viewForward, Object instance, String uri)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		for (Field field : fields) {
			if ((Modifier.isFinal(field.getModifiers())) || Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			field.setAccessible(true);
			String typeName = field.getName();
			Class<?> fieldType = field.getType();
			DateTimeFormat dateTimeFormat = fieldType.getAnnotation(DateTimeFormat.class);
			String pattern = null;
			if (dateTimeFormat != null) {
				pattern = dateTimeFormat.pattern();
				if (pattern == null || pattern.trim().equals("")) {
					DateTimeFormat.ISO iso = dateTimeFormat.iso();
					if (iso == DateTimeFormat.ISO.DATE) {
						pattern = DateTimeFormat.ISO_8601_DATE_PATTERN;
					} else if (iso == DateTimeFormat.ISO.TIME) {
						pattern = DateTimeFormat.ISO_8601_TIME_PATTERN;
					} else if (iso == DateTimeFormat.ISO.DATE_TIME) {
						pattern = DateTimeFormat.ISO_8601_DATE_TIME_PATTERN;
					}
				}
			}
			Object fieldValue = null;
			if (String.class == fieldType) {
				fieldValue = viewForward.paramString(typeName);
			} else if (ClassUtil.isSimpleValueType(fieldType)) {
				String value = viewForward.paramString(typeName);
				if (value == null && type.isPrimitive()) {
					throw new ConvertException(
							uri + ":" + typeName + " value null can't convert " + fieldType.getName() + "!");
				}
				try {
					fieldValue = ClassUtil.convert(value, fieldType);
				} catch (Exception e) {
					throw new ConvertException(
							uri + ":" + typeName + " value " + value + " can't convert " + fieldType.getName() + "!");
				}

			} else if (String[].class == fieldType) {
				fieldValue = viewForward.paramValues(typeName);
			} else if (ClassUtil.isSimpleArrayType(fieldType)) {
				String[] values = viewForward.paramValues(typeName);
				try {
					fieldValue = ClassUtil.convertArray(values, fieldType);
				} catch (Exception e) {
					throw new ConvertException(uri + ":" + typeName + " value " + Arrays.toString(values)
							+ " can't convert " + type.getName() + "!");
				}
				if (fieldValue == null && type.getComponentType().isPrimitive()) {
					throw new ConvertException(
							uri + ":" + typeName + " value null can't convert " + fieldType.getName() + "!");
				}
			} else if ((fieldValue = DateUtil.convertToDate(viewForward.paramString(typeName), pattern,
					type)) != null) {
			} else if (DateUtil.isDateTypeArray(fieldType)) {
				fieldValue = DateUtil.convertToDateArray(viewForward.paramValues(typeName), pattern,
						type.getComponentType());
			} else if (type.isArray() && type.getComponentType().isEnum()) {
				String[] values = viewForward.paramValues(typeName);
				if (values != null) {
					fieldValue = ClassUtil.createEnumArray((Class<Enum>) type.getComponentType(), values);
					try {
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + typeName + " " + e.getMessage());
					}
				}
			} else if (type.isEnum()) {
				String value = viewForward.paramString(typeName);
				if (value != null) {
					try {
						fieldValue = Enum.valueOf((Class<Enum>) type, value);
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + typeName + " " + e.getMessage());
					}
				}
			} else {
				field.set(instance, setField(fieldType, viewForward, null, uri));
			}
			if (fieldValue != null) {
				field.set(instance, fieldValue);
			}
		}
	}

}
