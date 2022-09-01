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
 * @date 2021年4月3日
 * @email suxuan696@gmail.com
 */
public class BasicsTypeMethodArgumentResolver extends AbstractMethodArgumentResolver {

	@Override
	public boolean isSupport(RequestParamFieldData requestParamFieldData) {
		Class<?> type = requestParamFieldData.getType();
		if (ClassUtil.isSimpleValueType(type) || String.class == type || String[].class == type
				|| Map.class.isAssignableFrom(type) || matchType(type)) {
			return true;
		}
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
		} else {
			return setFiled(type, viewForward, null);
		}

	}

	public boolean matchType(Class<?> type) {
		return !(type.getPackage().getName().startsWith("java.") || type.isInterface() || type.isEnum()
				|| type.isAnonymousClass() || Modifier.isAbstract(type.getModifiers()));
	}

	public Object setFiled(Class<?> type, ViewForward viewForward, Object instance)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if (!matchType(type) || type.isArray()) {
			return instance;
		}
		if (instance == null) {
			instance = SunReflectionFactoryInstantiator.newInstance(type);
		}
		Field[] fields = type.getDeclaredFields();
		for (Field filed : fields) {
			if ((Modifier.isFinal(filed.getModifiers())) || Modifier.isStatic(filed.getModifiers())) {
				continue;
			}
			filed.setAccessible(true);
			String typeName = filed.getName();
			Class<?> filedType = filed.getType();
			if (String.class == filedType) {
				filed.set(instance, viewForward.paramString(typeName));
			} else if (ClassUtil.isSimpleValueType(filedType)) {
				filed.set(instance, ClassUtil.convert(viewForward.paramString(typeName), filedType));
			} else if (String[].class == filedType) {
				filed.set(instance, viewForward.paramValues(typeName));
			} else if (ClassUtil.isSimpleArrayType(filedType)) {
				filed.set(instance, ClassUtil.convertArray(viewForward.paramValues(typeName), filedType));
			} else {
				filed.set(instance, setFiled(filedType, viewForward, null));
			}
		}
		return instance;
	}

}
