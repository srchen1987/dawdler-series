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
 * @Description 后台服务的配置类
 * @date 2007年7月22日
 * @email suxuan696@gmail.com
 */
public class XmlConfig {
	private static String config_path;
	private static final Logger logger = LoggerFactory.getLogger(XmlConfig.class);
	private static XmlObject xmlobject = null;

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
			throw new NullPointerException(config_path + "\tconfig/remote-load not found！");
		String path = ele.attributeValue("package").replace("${classpath}", DawdlerTool.getcurrentPath());
		return path;
	}

	private static void loadXML() {
		try {

			String config_path;
			File file;
			String activeProfile = System.getProperty("dawdler.profiles.active");
			String prefix = "services-config";
			String subfix = ".xml";
			config_path = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + subfix;
			file = new File(DawdlerTool.getcurrentPath() + config_path);
			if (!file.isFile()) {
				config_path = prefix + subfix;
				file = new File(DawdlerTool.getcurrentPath() + config_path);
			}
			if (!file.isFile()) {
				logger.error("not found services-config.xml");
			}
			xmlobject = XmlObject.loadClassPathXML(config_path);
		} catch (DocumentException | IOException e) {
			logger.error("", e);
		}
	}

}
