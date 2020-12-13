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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.dom4j.Element;
import org.dom4j.Node;

import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.ReflectionUtil;
import com.anywide.dawdler.util.XmlObject;

/**
 * 
 * @Title: DataSourceParser.java
 * @Description: 解析数据源配置的类
 * @author: jackson.song
 * @date: 2015年03月12日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class DataSourceParser {
	private static XmlObject datasourceConfig;
	static {
		File file = new File(DawdlerTool.getcurrentPath() + "../conf/datasources.xml");
		if (file.isFile()) {
			try {
				datasourceConfig = new XmlObject(file);
			} catch (Exception e) {
			}
		}
	}

	public static Map<String, DataSource> getDataSource(XmlObject xmlo, ClassLoader classLoader)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (xmlo == null)
			xmlo = datasourceConfig;
		if (xmlo == null)
			return null;
		Map<String, DataSource> datasources = new HashMap<>();
		List<Element> list = xmlo.selectNodes("/config/datasources/datasource");
		for (Element ele : list) {
			String id = ele.attributeValue("id");
			String code = ele.attributeValue("code");
			Class c = null;
			if (classLoader != null)
				c = classLoader.loadClass(code);
			else
				c = Class.forName(code);
			Object obj = c.newInstance();
			List<Node> attrs = ele.selectNodes("attribute");
			for (Node node : attrs) {
				Element e = (Element) node;
				String attributeName = e.attributeValue("name");
				String value = e.getText().trim();
				try {
					ReflectionUtil.invoke(obj, "set" + attributeName, Integer.parseInt(value));
				} catch (Exception ex) {
					ReflectionUtil.invoke(obj, "set" + attributeName, value);
				}

			}
			DataSource ds = (DataSource) obj;
			datasources.put(id, ds);
		}
		return datasources;
	}
}
