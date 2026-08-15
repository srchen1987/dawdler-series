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

import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

import club.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import club.dawdler.clientplug.web.exception.ConvertException;
import club.dawdler.clientplug.web.handler.ViewForward;
import club.dawdler.util.ClassUtil;
import club.dawdler.util.DateUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 获取基础类型、日期、Model相关参数值的决策者
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
		DateTimeFormatter formatter = requestParamFieldData.getFormatter();
		Object date = null;
		if (type == String.class || ClassUtil.isSimpleValueType(type)) {
			String value = viewForward.paramString(paramName);
			try {
				if ((value == null || "".equals(value.trim())) && type.isPrimitive()) {
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
		} else if ((date = DateUtil.convertToDate(viewForward.paramString(paramName), pattern, type,
				formatter)) != null) {
			return date;
		} else if (DateUtil.isDateTypeArray(type)) {
			return DateUtil.convertToDateArray(viewForward.paramValues(paramName), pattern, type.getComponentType(),
					formatter);
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

}
