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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClassUtil.java
 * @Package com.anywide.dawdler.util
 * @Description Class操作类
 * @date 2021年4月03日
 * @email suxuan696@gmail.com
 */
public class ClassUtil {
	private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);
	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);
	}

	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		return primitiveWrapperTypeMap.containsKey(clazz);
	}

	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}

	public static boolean isSimpleValueType(Class<?> type) {
		return ((isPrimitiveOrWrapper(type) || BigDecimal.class.isAssignableFrom(type)) && Void.class != type && void.class != type);
	}

	public static boolean isSimpleArrayType(Class<?> type) {
		return (type.isArray() && isSimpleValueType(type.getComponentType()));
	}

	public static boolean isSimpleProperty(Class<?> type) {
		return isSimpleValueType(type) || isSimpleArrayType(type);
	}

	public static <T extends Object> T convertArray(String[] value, Class<T> type) {
		if (value == null || value.length == 0)
			return null;
		if (!type.isArray())
			return null;
		Object array = null;
		if (type == String[].class) {
			return (T) value;
		} else if (type == int[].class) {
			array = new int[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Integer.parseInt(value[i]));
			}
		} else if (type == Integer[].class) {
			array = new Integer[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Integer.parseInt(value[i]));
			}
		} else if (type == long[].class) {
			array = new long[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Long.parseLong(value[i]));
			}
		} else if (type == Long[].class) {
			array = new Long[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Long.parseLong(value[i]));
			}
		} else if (type == double[].class) {
			array = new double[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Double.parseDouble(value[i]));
			}
		} else if (type == Double[].class) {
			array = new Double[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Double.parseDouble(value[i]));
			}
		} else if (type == boolean[].class) {
			array = new boolean[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Boolean.parseBoolean(value[i]));
			}
		} else if (type == Boolean[].class) {
			array = new Boolean[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Boolean.parseBoolean(value[i]));
			}
		} else if (type == short[].class) {
			array = new short[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Short.parseShort(value[i]));
			}
		} else if (type == Short[].class) {
			array = new Short[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Short.parseShort(value[i]));
			}
		} else if (type == byte[].class) {
			array = new byte[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Byte.parseByte(value[i]));
			}
		} else if (type == Byte[].class) {
			array = new Byte[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Byte.parseByte(value[i]));
			}
		} else if (type == float[].class) {
			array = new float[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Float.parseFloat(value[i]));
			}
		} else if (type == Float[].class) {
			array = new Float[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, Float.parseFloat(value[i]));
			}
		} else if (type == BigDecimal[].class) {
			array = new BigDecimal[value.length];
			for (int i = 0; i < value.length; i++) {
				Array.set(array, i, new BigDecimal(value[i]));
			}
		}
		return (T) array;
	}

	public static <T extends Object> T convert(Object value, Class<T> type) {
		if (value == null)
			return null;
		if (type == String.class) {
			value = value.toString();
		} else if (type == int.class || type == Integer.class) {
			value = Integer.parseInt(value.toString());
		} else if (type == long.class || type == Long.class) {
			value = Long.parseLong(value.toString());
		} else if (type == double.class || type == Double.class) {
			value = Double.parseDouble(value.toString());
		} else if (type == boolean.class || type == Boolean.class) {
			value = Boolean.parseBoolean(value.toString());
		} else if (type == short.class || type == Short.class) {
			value = Short.parseShort(value.toString());
		} else if (type == byte.class || type == Byte.class) {
			value = Byte.parseByte(value.toString());
		} else if (type == float.class || type == Float.class) {
			value = Float.parseFloat(value.toString());
		} else if (type == BigDecimal.class) {
			value = new BigDecimal((value.toString()));
		} else {
			return null;
		}
		return (T) value;
	}

}
