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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * xtream映射类
 */
public class ClientConfigParser {
	private static final Logger logger = LoggerFactory.getLogger(ClientConfigParser.class);
	private static ClientConfig config = null;
	private static XmlObject xmlObject;

	static {
		String fileName;
		String activeProfile = System.getProperty("dawdler.profiles.active");
		String prefix = "client-conf";
		String suffix = ".xml";
		InputStream xsdInput = ClientConfigParser.class.getResourceAsStream("/client-conf.xsd");
		fileName = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + suffix;
		InputStream input = DawdlerTool.getResourceFromClassPath(prefix, suffix);
		if (input == null) {
			logger.error("not found " + fileName);
		} else {
			try {
				xmlObject = new XmlObject(input, xsdInput);
				xmlObject.setPrefix("ns");
				config = new ClientConfig();
				Node certificatePath = xmlObject.selectSingleNode("/ns:config/ns:certificatePath");
				if (certificatePath != null) {
					config.setCertificatePath(certificatePath.getTextContent().trim());
				}
				List<Node> serverChannelGroupNode = xmlObject.selectNodes("/ns:config/ns:server-channel-group");
				for (Node node : serverChannelGroupNode) {
					ServerChannelGroup serverChannelGroup = config.new ServerChannelGroup();
					NamedNodeMap attributes = node.getAttributes();
					String groupId = getElementAttribute(attributes, "channel-group-id");
					int connectionNum = getElementAttribute2Int(attributes, "connection-num", 2);
					int sessionNum = getElementAttribute2Int(attributes, "session-num", 2);
					int serializer = getElementAttribute2Int(attributes, "serializer", 2);
					String user = getElementAttribute(attributes, "user");
					String password = getElementAttribute(attributes, "password");
					String host = getElementAttribute(attributes, "host");

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
			} finally {
				try {
					input.close();
				} catch (IOException e) {
				}
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
