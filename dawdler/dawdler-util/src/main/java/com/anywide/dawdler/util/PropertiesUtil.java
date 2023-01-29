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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author jackson.song
 * @version V1.0
 * @Title PropertiesUtil.java
 * @Description Class操作类
 * @date 2021年8月12日
 * @email suxuan696@gmail.com
 */
public class PropertiesUtil {
	private static Class<?> configMappingDataCacheClass = null;
	static {
		try {
			configMappingDataCacheClass = Thread.currentThread().getContextClassLoader().loadClass("com.anywide.dawdler.conf.cache.ConfigMappingDataCache");
		} catch (ClassNotFoundException e) {
		}
	}

	public static Properties loadPropertiesIfNotExistLoadConfigCenter(String fileName) throws Exception {
		Properties ps = null;
		if (configMappingDataCacheClass != null) {
			try {
				ps = PropertiesUtil.loadActiveProfileIfNotExistUseDefaultProperties(fileName);
			} catch (Exception e) {
				Method method = configMappingDataCacheClass.getMethod("getMappingDataCache", String.class);
				Map<String, Object> attributes = (Map<String, Object>) method.invoke(null, fileName);
				if (attributes == null) {
					throw e;
				}
				ps = new Properties();
				Set<Entry<String, Object>> entrySet = attributes.entrySet();
				for (Entry<String, Object> entry : entrySet) {
					if(entry.getValue() != null) {
						ps.setProperty(entry.getKey(), entry.getValue().toString());
					}
				}
			}
		} else {
			ps = PropertiesUtil.loadActiveProfileIfNotExistUseDefaultProperties(fileName);
		}
		return ps;
	}

	public static int getIfNullReturnDefaultValueInt(String key, int defaultValue, Properties ps) {
		Object value = ps.get(key);
		if (value != null) {
			try {
				return Integer.parseInt(value.toString());
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}

	public static long getIfNullReturnDefaultValueLong(String key, long defaultValue, Properties ps) {
		Object value = ps.get(key);
		if (value != null) {
			try {
				return Long.parseLong(value.toString());
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}

	public static boolean getIfNullReturnDefaultValueBoolean(String key, boolean defaultValue, Properties ps) {
		Object value = ps.get(key);
		if (value != null) {
			try {
				return Boolean.parseBoolean(value.toString());
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}

	public static Properties loadProperties(String fileName) throws Exception {
		String path = DawdlerTool.getCurrentPath() + fileName + ".properties";
		InputStream inStream = null;
		Properties ps = new Properties();
		try {
			inStream = new FileInputStream(path);
			ps.load(inStream);
		} finally {
			if (inStream != null) {
				inStream.close();
			}
		}
		if (ConfigContentDecryptor.useDecrypt()) {
			Properties processedPs = new Properties();
			Set<Entry<Object, Object>> entrySet = ps.entrySet();
			for (Entry<Object, Object> entry : entrySet) {
				processedPs.put(entry.getKey(),
						ConfigContentDecryptor.decryptAndReplaceTag(entry.getValue().toString()));
			}
			return processedPs;
		}
		return ps;
	}

	public static Properties loadActiveProfileProperties(String fileName) throws Exception {
		String activeProfile = System.getProperty("dawdler.profiles.active");
		if (activeProfile != null) {
			return loadProperties(fileName + "-" + activeProfile);
		}
		throw new IOException("dawdler.profiles.active not set!");
	}

	public static Properties loadActiveProfileIfNotExistUseDefaultProperties(String fileName) throws Exception {
		try {
			return loadActiveProfileProperties(fileName);
		} catch (Exception e) {
			return loadProperties(fileName);
		}
	}

}
