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
package com.anywide.dawdler.serverplug.db.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.util.DawdlerTool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DataAutomaticNewV2.java
 * @Description 反射实现的数据赋值工具
 * @date 2007年4月15日
 * @email suxuan696@gmail.com
 */
public class DataAutomaticNewV2 {
	private static final Map<String, Class<?>[]> DATA_TYPES = new HashMap<String, Class<?>[]>(64);
	private static final ConcurrentHashMap<Class<?>, Map<String, Method>> CACHE_METHOD = new java.util.concurrent.ConcurrentHashMap<Class<?>, Map<String, Method>>();

	static {
		init();
	}

	public static List<Map<String, Object>> buildMaps(ResultSet rs) throws SQLException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		while (rs.next()) {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			for (int j = 1; j <= numberOfColumns; j++) {
				String columnName = rsmd.getColumnLabel(j);
				Object obj = rs.getObject(columnName);
				if (obj != null) {
					map.put(columnName, rs.getObject(columnName));
				}
			}
			list.add(map);
		}
		return list;
	}

	public static <T extends Object> List<T> buildObject(Class<T> clazz, ResultSet rs) throws SQLException {
		List<T> list = new ArrayList<T>();
		if (rs == null) {
			return list;
		}
		T object = null;
		/*
		 * boolean isfirst=true; ResultSetMetaData rsmd = null; int numberOfColumns = 0;
		 */
		ResultSetMetaData rsmd = rs.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		while (rs.next()) {
			/*
			 * if(isfirst){ rsmd = rs.getMetaData(); numberOfColumns =
			 * rsmd.getColumnCount(); isfirst=false; }
			 */
			try {
				object = clazz.newInstance();
			} catch (Exception e) {
				return list;
			}
			for (int j = 1; j <= numberOfColumns; j++) {
				String columnName = rsmd.getColumnLabel(j);
				String typeName = rsmd.getColumnTypeName(j);
				String setMethodName = "set" + DawdlerTool.fnameToUpper(columnName);
				invoke(typeName, clazz, setMethodName, object, rs.getObject(columnName));
			}
			list.add(object);
		}
		return list;
	}

	private final static void init() {
		Class<?>[] byteclass = new Class[] { byte[].class };
		DATA_TYPES.put("TINYINT", new Class[] { boolean.class, Boolean.class, short.class, Short.class });
		DATA_TYPES.put("MEDIUMINT", new Class[] { int.class, Integer.class });
		DATA_TYPES.put("INTEGER", new Class[] { int.class, Integer.class });
		DATA_TYPES.put("INT", new Class[] { int.class, Integer.class });
		DATA_TYPES.put("BIGINT", new Class[] { long.class, Long.class });
		DATA_TYPES.put("FLOAT", new Class[] { float.class, Float.class });
		DATA_TYPES.put("DOUBLE", new Class[] { double.class, Double.class });
		DATA_TYPES.put("DECIMAL", new Class[] { java.math.BigDecimal.class, double.class, Double.class });
		DATA_TYPES.put("BIT", new Class[] { boolean.class, Boolean.class, byte.class, Byte.class });

		DATA_TYPES.put("DATE", new Class[] { java.sql.Date.class, String.class });
		DATA_TYPES.put("DATETIME", new Class[] { java.sql.Timestamp.class, String.class });
		DATA_TYPES.put("TIMESTAMP", new Class[] { java.sql.Timestamp.class, String.class });
		DATA_TYPES.put("TIME", new Class[] { java.sql.Time.class, String.class });
		DATA_TYPES.put("YEAR", new Class[] { java.sql.Date.class, String.class });
		DATA_TYPES.put("VARCHAR", new Class[] { String.class });
		DATA_TYPES.put("TEXT", new Class[] { String.class });

		DATA_TYPES.put("BINARY", byteclass);
		DATA_TYPES.put("TINYBLOB", byteclass);
		DATA_TYPES.put("BLOB", byteclass);
		DATA_TYPES.put("MEDIUMBLOB", byteclass);
		DATA_TYPES.put("LONGBLOB", byteclass);
		DATA_TYPES.put("VARBINARY", byteclass);
		DATA_TYPES.put("CHAR", new Class[] { String.class });
		DATA_TYPES.put("SMALLINT", new Class[] { short.class, Short.class, int.class, Integer.class });
	}

	private final static void invoke(String columntypename, Class<?> type, String setMethodName, Object obj,
			Object object) {
		Map<String, Method> methods = CACHE_METHOD.get(type);
		if (methods == null) {
			methods = new ConcurrentHashMap<String, Method>();
			Map<String, Method> pre = CACHE_METHOD.putIfAbsent(type, methods);
			if (pre != null) {
				methods = pre;
			}
		}
		Method method = methods.get(setMethodName);
		if (method == null) {
			Class<?>[] classtypes = DATA_TYPES.get(columntypename);
			if (object != null && classtypes != null) {
				for (Class<?> classtype : classtypes) {
					try {
						method = type.getMethod(setMethodName, classtype);
						method.setAccessible(true);
						methods.put(setMethodName, method);
						break;
					} catch (Exception e) {
						continue;
					}
				}
			}
		}
		try {
			if (method != null) {
				method.invoke(obj, object);
			}
		} catch (IllegalArgumentException e) {
			if ((columntypename.equals("SMALLINT") && object instanceof Integer)) {
				object = (Short.parseShort(object.toString()));
				try {
					method.invoke(obj, object);
				} catch (IllegalArgumentException e1) {
				} catch (IllegalAccessException e1) {
				} catch (InvocationTargetException e1) {
				}
			}
			/*
			 * try { if(object instanceof java.sql.Timestamp)
			 * method.invoke(obj,sdf.format(object)); } catch (Exception e2) { }
			 */
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}

}
