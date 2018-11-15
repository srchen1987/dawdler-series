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
import com.anywide.dawdler.util.DawdlerTool;
/**
 * 
 * @Title:  DataAutomaticNewV2.java   
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2007年04月15日       
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class DataAutomaticNewV2 {
	private static final Map<String, Class[]> map = new HashMap<String, Class[]>();
	private static final Map<Class, Map<String, Method>> cacheMethod = new java.util.concurrent.ConcurrentHashMap<Class, Map<String, Method>>();
	// private static java.text.SimpleDateFormat sdf = new
	// java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
				// String columnName = rsmd.getColumnName(j);
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
				// String columnName = rsmd.getColumnName(j);
				String columnName = rsmd.getColumnLabel(j);
				// String type = rsmd.getColumnClassName(j);
				String typename = rsmd.getColumnTypeName(j);
				// String firstLetter = columnName.substring(0, 1).toUpperCase();
				// String setMethodName = "set" + firstLetter + columnName.substring(1);
				String setMethodName = "set" + DawdlerTool.fnameToUpper(columnName);
				invoke(typename, clazz, setMethodName, object, rs.getObject(columnName));
			}
			list.add(object);
		}
		return list;
	}

	private final static void init() {
		Class[] byteclass = new Class[] { byte[].class };
		map.put("TINYINT", new Class[] { boolean.class, Boolean.class });
		map.put("MEDIUMINT", new Class[] { int.class, Integer.class });
		map.put("INTEGER", new Class[] { int.class, Integer.class });
		map.put("INT", new Class[] { int.class, Integer.class });
		map.put("BIGINT", new Class[] { long.class, Long.class });
		map.put("FLOAT", new Class[] { float.class, Float.class });
		map.put("DOUBLE", new Class[] { double.class, Double.class });
		map.put("DECIMAL", new Class[] { java.math.BigDecimal.class });

		map.put("DATE", new Class[] { java.sql.Date.class, String.class });
		map.put("DATETIME", new Class[] { java.sql.Timestamp.class, String.class });
		map.put("TIMESTAMP", new Class[] { java.sql.Timestamp.class, String.class });
		map.put("TIME", new Class[] { java.sql.Time.class, String.class });
		map.put("YEAR", new Class[] { java.sql.Date.class, String.class });
		map.put("VARCHAR", new Class[] { String.class });

		map.put("BINARY", byteclass);
		map.put("TINYBLOB", byteclass);
		map.put("BLOB", byteclass);
		map.put("MEDIUMBLOB", byteclass);
		map.put("LONGBLOB", byteclass);
		map.put("VARBINARY", byteclass);
		map.put("CHAR", new Class[] { String.class });
		map.put("SMALLINT", new Class[] { short.class, Short.class, int.class, Integer.class });
	}

	private final static void invoke(String columntypename, Class classbean, String setMethodName, Object obj,
			Object object) {
		Map<String, Method> methods = cacheMethod.get(classbean);
		if (methods == null) {
			methods = new HashMap<String, Method>();
			cacheMethod.put(classbean, methods);
		}
		Method method = methods.get(setMethodName);
		if (method == null) {
			Class[] classtypes = map.get(columntypename);
			if (object != null && classtypes != null) {
				for (Class classtype : classtypes) {
					try {
						method = classbean.getMethod(setMethodName, classtype);
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
			/*
			 * if(columntypename.equals("SMALLINT")&&object instanceof Integer){ object =
			 * (Short.parseShort(""+object)); }
			 */
			/*
			 * else if(columntypename.equals("TINYINT")&&object instanceof Boolean){
			 * if(classtype.isAssignableFrom(short.class)||classtype.isAssignableFrom(Short.
			 * class)){ boolean b =(Boolean)object; if(b) object =(short)1; else object
			 * =(short)0; } }
			 */
			// else if(columntypename.equals("TINYINT")&&object instanceof Boolean){
			// if(classtype.isAssignableFrom(short.class)||classtype.isAssignableFrom(Short.class)){
			// boolean b =(Boolean)object;
			// if(b)
			// object =(short)1;
			// else
			// object =(short)0;
			// }
			// /*else
			// if(classtype.isAssignableFrom(boolean.class)||classtype.isAssignableFrom(Boolean.class)){
			// if(object.equals(0))object=false;
			// else object=true;
			// }*/
			// }
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
