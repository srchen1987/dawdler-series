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
import java.util.Map;

import com.anywide.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.util.ClassUtil;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;

/**
 * @author jackson.song
 * @version V1.0
 * @Title BasicsTypeMethodArgumentResolver.java
 * @Description 获取基础类型或Model相关参数值的决策者
 * @date 2021年04月03日
 * @email suxuan696@gmail.com
 */
public class BasicsTypeMethodArgumentResolver extends AbstractMethodArgumentResolver {

	@Override
	public boolean isSupport(RequestParamFieldData requestParamFieldData) {
		Class<?> type = requestParamFieldData.getType();
		if (ClassUtil.isSimpleValueType(type) || String.class == type || String[].class == type
				|| Map.class.isAssignableFrom(type))
			return true;

		return false;

	}

	@Override
	public Object resolveArgument(RequestParamFieldData requestParamFieldData, ViewForward viewForward)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Class<?> type = requestParamFieldData.getType();
		String paramName = getParameterName(requestParamFieldData);
		if (String.class == type) {
			return viewForward.paramString(paramName);
		} else if (ClassUtil.isSimpleValueType(type)) {
			return ClassUtil.convert(viewForward.paramString(paramName), type);
		} else if (String[].class == type) {
			return viewForward.paramValues(paramName);
		} else if (ClassUtil.isSimpleArrayType(type)) {
			return ClassUtil.convertArray(viewForward.paramValues(paramName), type);
		} else if (Map.class.isAssignableFrom(type)) {
			return viewForward.paramMaps();
		} else if (type.getClassLoader() != null) {
			Object instance = SunReflectionFactoryInstantiator.newInstance(type);
			Field[] fields = type.getDeclaredFields();
			for (Field filed : fields) {
				filed.setAccessible(true);
				String typeName = type.getName();
				if (String.class == type) {
					filed.set(instance, viewForward.paramString(typeName));
				} else if (ClassUtil.isSimpleValueType(type)) {
					filed.set(instance, ClassUtil.convert(viewForward.paramString(typeName), type));
				} else if (String[].class == type) {
					filed.set(instance, viewForward.paramValues(typeName));
				} else if (ClassUtil.isSimpleArrayType(type)) {
					filed.set(instance, ClassUtil.convertArray(viewForward.paramValues(typeName), type));
				}
			}
			return instance;
		} else {
			return null;
		}

	}

}
