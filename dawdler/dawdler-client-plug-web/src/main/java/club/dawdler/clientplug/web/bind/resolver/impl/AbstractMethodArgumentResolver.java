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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import club.dawdler.clientplug.web.annotation.DateTimeFormat;
import club.dawdler.clientplug.web.annotation.RequestParam;
import club.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import club.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;
import club.dawdler.clientplug.web.exception.ConvertException;
import club.dawdler.clientplug.web.handler.ViewForward;
import club.dawdler.util.ClassUtil;
import club.dawdler.util.DateUtil;
import club.dawdler.util.SunReflectionFactoryInstantiator;

/**
 * @author jackson.song
 * @version V1.0
 * 获取参数值的决策者的抽象类
 */
public abstract class AbstractMethodArgumentResolver implements MethodArgumentResolver {

	protected String getParameterName(RequestParamFieldData requestParamFieldData) {
		RequestParam requestParam = requestParamFieldData.getAnnotation(RequestParam.class);
		String paramName = null;
		if (requestParam != null) {
			paramName = requestParam.value();
		}
		if (paramName == null || paramName.trim().equals("")) {
			paramName = requestParamFieldData.getParamName();
		}
		return paramName;
	}

	protected String getParameterName(String paramName, RequestParamFieldData requestParamFieldData) {
		if (paramName == null || paramName.trim().equals("")) {
			paramName = requestParamFieldData.getParamName();
		}
		return paramName;
	}

	protected boolean matchType(Class<?> type) {
		if (type == null || type.isPrimitive()) {
			return false;
		}
		if (type.isArray()) {
			type = type.getComponentType();
			if (type.isPrimitive()) {
				return false;
			}
		}
		Package pkg = type.getPackage();
		if (pkg == null || pkg.getName().startsWith("java.")) {
			return false;
		}
		return !type.isInterface() && !type.isAnonymousClass() && !Modifier.isAbstract(type.getModifiers());
	}

	protected Object setField(Class<?> type, ViewForward viewForward, Object instance, String uri)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if (!matchType(type) || type.isArray()) {
			return instance;
		}
		if (instance == null) {
			instance = SunReflectionFactoryInstantiator.newInstance(type);
		}

		do {
			setField(type.getDeclaredFields(), viewForward, instance, uri);
			type = type.getSuperclass();
		} while (type != null && type != Object.class);

		return instance;
	}

	@SuppressWarnings("unchecked")
	private void setField(Field[] fields, ViewForward viewForward, Object instance, String uri)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		for (Field field : fields) {
			if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			field.setAccessible(true);
			String fieldName = field.getName();
			Class<?> fieldType = field.getType();
			DateTimeFormat dateTimeFormat = field.getAnnotation(DateTimeFormat.class);
			String pattern = null;
			DateTimeFormatter formatter = null;
			if (dateTimeFormat != null) {
				pattern = dateTimeFormat.pattern();
				if (dateTimeFormat.iso() == DateTimeFormat.ISO.BASED) {
					formatter = DateUtil.getISODateTimeFormatter(fieldType);
				}
			}
			Object fieldValue = null;
			if (String.class == fieldType) {
				fieldValue = viewForward.paramString(fieldName);
			} else if (ClassUtil.isSimpleValueType(fieldType)) {
				String value = viewForward.paramString(fieldName);
				if (value == null && fieldType.isPrimitive()) {
					throw new ConvertException(
							uri + ":" + fieldName + " value null can't convert " + fieldType.getName() + "!");
				}
				try {
					fieldValue = ClassUtil.convert(value, fieldType);
				} catch (Exception e) {
					throw new ConvertException(
							uri + ":" + fieldName + " value " + value + " can't convert " + fieldType.getName() + "!");
				}
			} else if (String[].class == fieldType) {
				fieldValue = viewForward.paramValues(fieldName);
			} else if (ClassUtil.isSimpleArrayType(fieldType)) {
				String[] values = viewForward.paramValues(fieldName);
				try {
					fieldValue = ClassUtil.convertArray(values, fieldType);
				} catch (Exception e) {
					throw new ConvertException(uri + ":" + fieldName + " value " + Arrays.toString(values)
							+ " can't convert " + fieldType.getName() + "!");
				}
				if (fieldValue == null && fieldType.getComponentType().isPrimitive()) {
					throw new ConvertException(
							uri + ":" + fieldName + " value null can't convert " + fieldType.getName() + "!");
				}
			} else if ((fieldValue = DateUtil.convertToDate(viewForward.paramString(fieldName), pattern, fieldType,
					formatter)) != null) {
			} else if (DateUtil.isDateTypeArray(fieldType)) {
				fieldValue = DateUtil.convertToDateArray(viewForward.paramValues(fieldName), pattern,
						fieldType.getComponentType(), formatter);
			} else if (fieldType.isArray() && fieldType.getComponentType().isEnum()) {
				String[] values = viewForward.paramValues(fieldName);
				if (values != null) {
					try {
						fieldValue = ClassUtil.createEnumArray((Class<Enum>) fieldType.getComponentType(), values);
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + fieldName + " value " + Arrays.toString(values) + " can't convert "
										+ fieldType.getName() + "!");
					}
				}
			} else if (fieldType.isEnum()) {
				String value = viewForward.paramString(fieldName);
				if (value != null) {
					try {
						fieldValue = Enum.valueOf((Class<Enum>) fieldType, value);
					} catch (Exception e) {
						throw new ConvertException(
								uri + ":" + fieldName + " value " + value + " can't convert " + fieldType.getName()
										+ "!");
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
