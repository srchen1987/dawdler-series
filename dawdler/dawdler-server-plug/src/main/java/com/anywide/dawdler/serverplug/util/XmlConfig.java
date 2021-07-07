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
package com.anywide.dawdler.serverplug.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title XmlConfig.java
 * @Description 简单的xml操作类
 * @date 2007年07月22日
 * @email suxuan696@gmail.com
 */
public class XmlConfig {
	private static final String CONFIG_PATH = "services-config.xml";
	private static final Logger logger = LoggerFactory.getLogger(XmlConfig.class);
	private static XmlObject xmlobject = null;
	private static final Map<String, Map<String, String>> datas = Collections.synchronizedMap(new HashMap<>());

	static {
		loadXML();
	}

	private XmlConfig() {
	}

	public static XmlObject getConfig() {
		return xmlobject;
	}

	public static String getRemoteLoad() {
		Element ele = (Element) xmlobject.getRoot().selectSingleNode("/config/remote-load");
		if (ele == null)
			throw new NullPointerException(CONFIG_PATH + "config/remote-load not found！");
		String path = ele.attributeValue("package").replace("${classpath}", DawdlerTool.getcurrentPath());
		return path;
	}

	public static Map<String, Map<String, String>> getDatas() {
		return datas;
	}

	private static void loadXML() {
		try {
			xmlobject = XmlObject.loadClassPathXML(File.separator + CONFIG_PATH);
		} catch (DocumentException | IOException e) {
			logger.error("", e);
		}
		loadDataSource();
	}

	private static void loadDataSource() {
		List<Element> list = xmlobject.selectNodes("/config/server-datas/server-data");
		for (Iterator<Element> it = list.iterator(); it.hasNext();) {
			Element ele = it.next();
			Map<String, String> items = new HashMap<>();
			for (Iterator<Attribute> its = ele.attributes().iterator(); its.hasNext();) {
				Attribute ab = its.next();
				items.put(ab.getName(), ab.getValue());
			}
			datas.put(ele.attributeValue("id"), items);
		}
	}

}
