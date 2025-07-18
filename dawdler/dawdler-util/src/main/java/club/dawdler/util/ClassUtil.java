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
package club.dawdler.util;

import java.math.BigDecimal;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * Class操作类
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
		return ((isPrimitiveOrWrapper(type) || BigDecimal.class.isAssignableFrom(type)) && Void.class != type
				&& void.class != type);
	}

	public static boolean isSimpleArrayType(Class<?> type) {
		return (type.isArray() && isSimpleValueType(type.getComponentType()));
	}

	public static boolean isSimpleProperty(Class<?> type) {
		return isSimpleValueType(type) || isSimpleArrayType(type);
	}

	public static <T extends Object> T convertArray(String[] value, Class<T> type) {
		if (value == null || value.length == 0) {
			return null;
		}
		if (!type.isArray()) {
			return null;
		}
		Object result = null;
		if (type == String[].class) {
			return (T) value;
		} else if (type == int[].class) {
			int[] array = new int[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Integer.parseInt(value[i]);
			}
			result = array;
		} else if (type == Integer[].class) {
			Integer[] array = new Integer[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Integer.parseInt(value[i]);
			}
			result = array;
		} else if (type == long[].class) {
			long[] array = new long[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Long.parseLong(value[i]);
			}
			result = array;
		} else if (type == Long[].class) {
			Long[] array = new Long[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Long.parseLong(value[i]);
			}
			result = array;
		} else if (type == double[].class) {
			double[] array = new double[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Double.parseDouble(value[i]);
			}
			result = array;
		} else if (type == Double[].class) {
			Double[] array = new Double[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Double.parseDouble(value[i]);
			}
			result = array;
		} else if (type == boolean[].class) {
			boolean[] array = new boolean[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Boolean.parseBoolean(value[i]);
			}
			result = array;
		} else if (type == Boolean[].class) {
			Boolean[] array = new Boolean[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Boolean.parseBoolean(value[i]);
			}
			result = array;
		} else if (type == short[].class) {
			short[] array = new short[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Short.parseShort(value[i]);
			}
			result = array;
		} else if (type == Short[].class) {
			Short[] array = new Short[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Short.parseShort(value[i]);
			}
			result = array;
		} else if (type == byte[].class) {
			byte[] array = new byte[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Byte.parseByte(value[i]);
			}
			result = array;
		} else if (type == Byte[].class) {
			Byte[] array = new Byte[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Byte.parseByte(value[i]);
			}
			result = array;
		} else if (type == float[].class) {
			float[] array = new float[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Float.parseFloat(value[i]);
			}
			result = array;
		} else if (type == Float[].class) {
			Float[] array = new Float[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = Float.parseFloat(value[i]);
			}
			result = array;
		} else if (type == char[].class) {
			char[] array = new char[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = value[i].charAt(0);
			}
			result = array;
		} else if (type == Character[].class) {
			Character[] array = new Character[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = value[i].charAt(0);
			}
			result = array;
		} else if (type == BigDecimal[].class) {
			BigDecimal[] array = new BigDecimal[value.length];
			for (int i = 0; i < value.length; i++) {
				array[i] = BigDecimal.valueOf(Double.parseDouble(value[i]));
			}
			result = array;
		}
		return (T) result;
	}

	public static <T extends Object> T convert(Object value, Class<T> type) {
		if (value == null) {
			return null;
		}
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
		} else if (type == char.class || type == Character.class) {
			value = value.toString().charAt(0);
		} else if (type == BigDecimal.class) {
			value = new BigDecimal((value.toString()));
		} else {
			return null;
		}
		return (T) value;
	}

	public static String[] convertSimpleArrayToStringArray(Object value) {
		if (value == null || !value.getClass().isArray()) {
			return null;
		}
		String[] result = null;
		Class<?> type = value.getClass();
		if (type == int[].class) {
			int[] array = (int[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == Integer[].class) {
			Integer[] array = (Integer[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == long[].class) {
			long[] array = (long[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == Long[].class) {
			Long[] array = (Long[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == double[].class) {
			double[] array = (double[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == Double[].class) {
			Double[] array = (Double[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == boolean[].class) {
			boolean[] array = (boolean[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == Boolean[].class) {
			Boolean[] array = (Boolean[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == short[].class) {
			short[] array = (short[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == Short[].class) {
			Short[] array = (Short[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == byte[].class) {
			byte[] array = (byte[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == Byte[].class) {
			Byte[] array = (Byte[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == float[].class) {
			float[] array = (float[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == Float[].class) {
			Float[] array = (Float[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == char[].class) {
			char[] array = (char[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == Character[].class) {
			Character[] array = (Character[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		} else if (type == BigDecimal[].class) {
			BigDecimal[] array = (BigDecimal[]) value;
			result = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = String.valueOf(array[i]);
			}
			return result;
		}
		return result;
	}

}
