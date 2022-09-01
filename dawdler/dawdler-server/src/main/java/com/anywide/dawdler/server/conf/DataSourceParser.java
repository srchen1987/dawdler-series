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
package com.anywide.dawdler.server.conf;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.ReflectionUtil;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DataSourceParser.java
 * @Description 解析数据源配置的类
 * @date 2015年3月12日
 * @email suxuan696@gmail.com
 */
public class DataSourceParser {
	private static XmlObject dataSourceConfig;
	private static Map<String, DataSource> dataSources;
	private static final Logger logger = LoggerFactory.getLogger(DataSourceParser.class);
	static {
		File file = new File(DawdlerTool.getCurrentPath() + "../conf/data-sources.xml");
		if (file.isFile()) {
			try {
				dataSourceConfig = new XmlObject(file);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	public static Map<String, DataSource> getDataSource(XmlObject xmlo, ClassLoader classLoader)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		if (xmlo == null) {
			xmlo = dataSourceConfig;
		}
		if (xmlo == null) {
			return null;
		}
		dataSources = new HashMap<>(8);
		List<Node> dataSourceList = xmlo.selectNodes("/config/datasources/datasource");
		for (Object dataSource : dataSourceList) {
			Map<String, Object> attributes = new HashMap<>(16);
			Element ele = (Element) dataSource;
			String id = ele.attributeValue("id");
			List<Node> attrs = ele.selectNodes("attribute");
			for (Node node : attrs) {
				Element e = (Element) node;
				String attributeName = e.attributeValue("name").trim();
				String value = e.getText().trim();
				attributes.put(attributeName, value);
			}
			initDataSources(id, attributes);
		}
		return dataSources;
	}

	private static void initDataSources(String id, Map<String, Object> attributes)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		if (dataSources.containsKey(id)) {
			return;
		}
		String type = (String) attributes.remove("type");
		if (type == null) {
			throw new NullPointerException("dataSource attribute [type] can't be null!");
		}
		Class<?> clazz = Class.forName(type);
		Object obj = clazz.getDeclaredConstructor().newInstance();

		attributes.forEach((k, v) -> {
			String attributeName = captureName(k);
			try {
				ReflectionUtil.invoke(obj, "set" + attributeName, v.toString());
			} catch (Exception ex) {
				try {
					ReflectionUtil.invoke(obj, "set" + attributeName, Integer.parseInt(v.toString()));
				} catch (Exception exception) {
					ReflectionUtil.invoke(obj, "set" + attributeName, Long.parseLong(v.toString()));
				}
			}
		});

		DataSource ds = (DataSource) obj;
		dataSources.put(id, ds);
	}

	public static Map<String, DataSource> getDataSources() {
		return dataSources;
	}

	private static String captureName(String str) {
		char[] cs = str.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);
	}

}
