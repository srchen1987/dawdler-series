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
package com.anywide.dawdler.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.util.reflectasm.MethodAccess;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ReflectionUtil.java
 * @Description 反射工具类
 * @date 2007年3月12日
 * @email suxuan696@gmail.com
 */
public class ReflectionUtil {
	private static ConcurrentHashMap<Class<?>, MethodAccess> methodAccessCache = new ConcurrentHashMap<>();
	
	public static Object invoke(Object object, String methodName, Object... args) {
		if (object == null) {
			throw new IllegalArgumentException("object can not be null.");
		}
		if (methodName == null || "".equals(methodName)) {
			throw new IllegalArgumentException("methodName can not be null or empty.");
		}
		MethodAccess methodAccess = getMethodAccess(object);
		return methodAccess.invoke(object, methodName, args);
	}

	public static Object invoke(MethodAccess methodAccess, Object object, int methodIndex, Object... args) {
		Object result = null;
		try {
			result = methodAccess.invoke(object, methodIndex, args);
		} catch (Throwable e) {
			throw e;
		}
		return result;
	}

	public static MethodAccess getMethodAccess(Object object) {
		return getMethodAccess(object.getClass());
	}

	public static MethodAccess getMethodAccess(Class<?> objectClass) {
		MethodAccess methodAccess = methodAccessCache.get(objectClass);
		if (methodAccess == null) {
			methodAccess = MethodAccess.get(objectClass);
			MethodAccess preMethodAccess = methodAccessCache.putIfAbsent(objectClass, methodAccess);
			if (preMethodAccess != null)
				methodAccess = preMethodAccess;
		}
		return methodAccess;
	}

	public static int getMethodIndex(MethodAccess methodAccess, String methodName, Class<?>... paramTypes) {
		return methodAccess.getIndex(methodName, paramTypes);
	}

	public static Object invoke(Object object, Method method, Object... args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return method.invoke(object, args);
	}

	public static Object getDefaultValue(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			if (byte.class.equals(clazz)) {
				return (byte) 0;
			} else if (short.class.equals(clazz)) {
				return (short) 0;
			} else if (int.class.equals(clazz)) {
				return 0;
			} else if (long.class.equals(clazz)) {
				return 0L;
			} else if (float.class.equals(clazz)) {
				return 0F;
			} else if (double.class.equals(clazz)) {
				return 0D;
			} else if (char.class.equals(clazz)) {
				return (char) 0;
			} else if (boolean.class.equals(clazz)) {
				return false;
			}
		}
		return null;
	}

	public static boolean isMatch(Object[] params, Class<?>... paramTypes) {
		if (params == null) {
			return paramTypes == null;
		} else if (paramTypes == null) {
			return false;
		}

		if (params.length == paramTypes.length) {
			for (int i = 0; i < params.length; i++) {
				if (!isMatch(params[i], paramTypes[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isMatch(Object param, Class<?> paramType) {
		if (param == null) {
			if (paramType != null) {
				return !paramType.isPrimitive();
			}
			return true;
		}

		if (paramType == null) {
			throw new NullPointerException("paramType can't be null.");
		}

		boolean match = paramType.isInstance(param);

		if (!match) {
			if (param.getClass().isArray()) {
				return isMatch((Object[]) param, new Class<?>[] { paramType });
			} else if (byte.class.equals(paramType)) {
				match = Byte.class.isInstance(param);
			} else if (short.class.equals(paramType)) {
				match = Short.class.isInstance(param);
			} else if (int.class.equals(paramType)) {
				match = Integer.class.isInstance(param);
			} else if (long.class.equals(paramType)) {
				match = Long.class.isInstance(param);
			} else if (float.class.equals(paramType)) {
				match = Float.class.isInstance(param);
			} else if (double.class.equals(paramType)) {
				match = Double.class.isInstance(param);
			} else if (char.class.equals(paramType)) {
				match = Character.class.isInstance(param);
			} else if (boolean.class.equals(paramType)) {
				match = Boolean.class.isInstance(param);
			}
		}
		return match;
	}

	public static Field getField(Class<?> clazz, String fdName) throws NoSuchFieldException {
		Class<?> cls = clazz;
		while (cls != null) {
			try {
				Field fd = cls.getDeclaredField(fdName);
				return fd;
			} catch (Throwable t) {
			} finally {
				cls = cls.getSuperclass();
			}
		}

		throw new NoSuchFieldException(fdName);
	}

	public static List<Field> getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		Class<?> cls = clazz;
		while (cls != null) {
			try {
				Field[] fdArray = cls.getDeclaredFields();
				for (Field field : fdArray) {
					fields.add(field);
				}
			} catch (Throwable t) {
			} finally {
				cls = cls.getSuperclass();
			}
		}
		return fields;
	}
}
