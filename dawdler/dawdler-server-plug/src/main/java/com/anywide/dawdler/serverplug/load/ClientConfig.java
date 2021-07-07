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
package com.anywide.dawdler.serverplug.load;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientConfig.java
 * @Description 客户端配置文件解析器
 * @date 2007年05月22日
 * @email suxuan696@gmail.com
 */
public class ClientConfig {
	private static final String SRC_CONFIG = "client/client-conf.xml";
	private static final ClientConfig remoteFactory = new ClientConfig();
	private static final Logger logger = LoggerFactory.getLogger(ClientConfig.class);
	private static long updateTime = 0;
	private static XmlObject xml = null;
	private static File file = null;

	static {
		try {
			file = new File(DawdlerTool.getcurrentPath() + SRC_CONFIG);
//			isUpdate();
			try {
				xml = XmlObject.loadClassPathXML(SRC_CONFIG);
			} catch (IOException e) {
				logger.error("", e);
			}
		} catch (DocumentException e) {
			logger.error("", e);
		}
	}

	private ClientConfig() {
	}

	public static ClientConfig getInstance() {
		return remoteFactory;
	}

	private static boolean isUpdate() {
		if (!file.exists()) {
			System.err.println("not found " + SRC_CONFIG);
			return false;
		}
		if (updateTime != file.lastModified()) {
			updateTime = file.lastModified();
			return true;
		}
		return false;
	}

	public XmlObject getXml() {
		if (isUpdate()) {
			try {
				xml = XmlObject.loadClassPathXML(SRC_CONFIG);
			} catch (DocumentException e) {
				logger.error("", e);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		return xml;
	}
}
