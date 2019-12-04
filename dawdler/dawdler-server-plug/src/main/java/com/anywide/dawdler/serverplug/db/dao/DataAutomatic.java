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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataAutomatic {
	public static <T extends Object> List<T> buildObject(Class<T> clazz, ResultSet rs) {
		if (rs == null) {
			return null;
		}
		List<T> list = new ArrayList<T>();
		T object = null;
		try {
			Field[] fields = clazz.getDeclaredFields();
			while (rs.next()) {
				object = clazz.newInstance();
				for (int i = 0; i < fields.length; i++) {
					String fieldName = fields[i].getName();
					String firstLetter = fieldName.substring(0, 1).toUpperCase();
					String setMethodName = "set" + firstLetter + fieldName.substring(1);
					Method setMethod = clazz.getMethod(setMethodName, new Class[] { fields[i].getType() });
					try {
						setMethod.invoke(object, new Object[] { rs.getObject(fieldName.toLowerCase()) });
					} catch (Exception e) {
						try {
							setMethod.invoke(object, new Object[] { rs.getString((fieldName.toLowerCase())) });
						} catch (Exception e1) {
							try {
								Class c = fields[i].getType();
								if (c.isAssignableFrom(int.class) || c.isAssignableFrom(Integer.class)) {
									setMethod.invoke(object, new Object[] { rs.getInt((fieldName.toLowerCase())) });
								} else if (c.isAssignableFrom(short.class) || c.isAssignableFrom(Short.class)) {
									setMethod.invoke(object, new Object[] { rs.getShort((fieldName.toLowerCase())) });
								} else if (c.isAssignableFrom(long.class) || c.isAssignableFrom(Long.class)) {
									setMethod.invoke(object, new Object[] { rs.getLong((fieldName.toLowerCase())) });
								} else if (c.isAssignableFrom(byte.class) || c.isAssignableFrom(Byte.class)) {
									setMethod.invoke(object, new Object[] { rs.getByte((fieldName.toLowerCase())) });
								} else if (c.isAssignableFrom(byte[].class) || c.isAssignableFrom(Byte[].class)) {
									setMethod.invoke(object, new Object[] { rs.getBytes((fieldName.toLowerCase())) });
								} else if (c.isAssignableFrom(double.class) || c.isAssignableFrom(Double.class)) {
									setMethod.invoke(object, new Object[] { rs.getDouble((fieldName.toLowerCase())) });
								} else if (c.isAssignableFrom(float.class) || c.isAssignableFrom(Float.class)) {
									setMethod.invoke(object, new Object[] { rs.getFloat((fieldName.toLowerCase())) });
								}
							} catch (Exception e2) {
							}
						}

					}
				}
				list.add(object);
			}
		} catch (Exception e) {
			e.printStackTrace();
			object = null;
		}
		return list;
	}

	public static <T extends Object> List<T> buildObjectNew(Class<T> clazz, ResultSet rs) {
		ResultSetMetaData md;
		try {
			md = rs.getMetaData();
			int count = md.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i < count + 1; i++) {
					md.getColumnType(i);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}
}
