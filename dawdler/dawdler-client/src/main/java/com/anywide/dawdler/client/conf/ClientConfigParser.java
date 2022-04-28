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
package com.anywide.dawdler.client.conf;

import static com.anywide.dawdler.util.XmlObject.getElementAttribute;
import static com.anywide.dawdler.util.XmlObject.getElementAttribute2Int;

import java.io.File;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientConfigParser.java
 * @Description xtream映射类
 * @date 2015年3月16日
 * @email suxuan696@gmail.com
 */
public class ClientConfigParser {
	private static final Logger logger = LoggerFactory.getLogger(ClientConfigParser.class);
	private static ClientConfig config = null;
	private static XmlObject xmlObject;

	static {
		String fileName;
		File file;
		String activeProfile = System.getProperty("dawdler.profiles.active");
		String prefix = "client/client-conf";
		String subfix = ".xml";
		fileName = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + subfix;
		file = new File(DawdlerTool.getcurrentPath() + fileName);
		if (!file.isFile()) {
			logger.error("not found " + fileName);
		} else {
			try {
				xmlObject = new XmlObject(DawdlerTool.getcurrentPath() + fileName);
				Element root = xmlObject.getRoot();
				config = new ClientConfig();
				Element zkHost = (Element) root.selectSingleNode("zk-host");
				if (zkHost != null) {
					config.setZkHost(zkHost.getTextTrim());
					config.setZkUsername(zkHost.attributeValue("username"));
					config.setZkPassword(zkHost.attributeValue("password"));
				}

				Node certificatePath = root.selectSingleNode("certificatePath");
				if (certificatePath != null) {
					config.setCertificatePath(certificatePath.getText());
				}

				List<Node> serverChannelGroupNode = root.selectNodes("server-channel-group");
				for (Node node : serverChannelGroupNode) {
					ServerChannelGroup serverChannelGroup = config.new ServerChannelGroup();

					Element serverChannelGroupEle = (Element) node;
					String groupId = getElementAttribute(serverChannelGroupEle, "channel-group-id");
					int connectionNum = getElementAttribute2Int(serverChannelGroupEle, "connection-num", 2);
					int sessionNum = getElementAttribute2Int(serverChannelGroupEle, "session-num", 2);
					int serializer = getElementAttribute2Int(serverChannelGroupEle, "serializer", 2);
					String user = getElementAttribute(serverChannelGroupEle, "user");
					String password = getElementAttribute(serverChannelGroupEle, "password");
					String host = getElementAttribute(serverChannelGroupEle, "host");

					serverChannelGroup.setGroupId(groupId);
					serverChannelGroup.setConnectionNum(connectionNum);
					serverChannelGroup.setSessionNum(sessionNum);
					serverChannelGroup.setSerializer(serializer);
					serverChannelGroup.setUser(user);
					serverChannelGroup.setPassword(password);
					serverChannelGroup.setHost(host);
					config.getServerChannelGroups().add(serverChannelGroup);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	public static ClientConfig getClientConfig() {
		return config;
	}

	public static XmlObject getXmlObject() {
		return xmlObject;
	}
}
