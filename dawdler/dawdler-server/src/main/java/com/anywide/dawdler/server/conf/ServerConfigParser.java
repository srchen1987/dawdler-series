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

import static com.anywide.dawdler.util.XmlTool.getElementAttribute;
import static com.anywide.dawdler.util.XmlTool.getElementAttribute2Boolean;
import static com.anywide.dawdler.util.XmlTool.getElementAttribute2Int;
import static com.anywide.dawdler.util.XmlTool.getElementAttribute2Long;
import static com.anywide.dawdler.util.XmlTool.getNodes;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.server.conf.ServerConfig.HealthCheck;
import com.anywide.dawdler.server.conf.ServerConfig.KeyStore;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServerConfigParser.java
 * @Description 服务器配置解析类 (抛弃老版本的xstream实现，后抛弃dom4j实现)
 * @date 2015年4月4日
 * @email suxuan696@gmail.com
 */
public class ServerConfigParser {
	private ServerConfig serverConfig = new ServerConfig();

	public void loadKeyStore(Node keyStoreEle) {
		NamedNodeMap namedNodeMap = keyStoreEle.getAttributes();
		String keyStorePath = getElementAttribute(namedNodeMap, "keyStorePath");
		String alias = getElementAttribute(namedNodeMap, "alias");
		String password = getElementAttribute(namedNodeMap, "password");
		KeyStore keyStore = serverConfig.getKeyStore();
		keyStore.setKeyStorePath(keyStorePath);
		keyStore.setAlias(alias);
		keyStore.setPassword(password);
	}

	public void loadServer(Node serverEle) {
		NamedNodeMap namedNodeMap = serverEle.getAttributes();
		Server server = serverConfig.getServer();
		server.setHost(getElementAttribute(namedNodeMap, "host", server.getHost()));
		server.setTcpPort(getElementAttribute2Int(namedNodeMap, "tcp-port", server.getTcpPort()));
		server.setTcpBacklog(getElementAttribute2Int(namedNodeMap, "tcp-backlog", server.getTcpBacklog()));
		server.setTcpSendBuffer(getElementAttribute2Int(namedNodeMap, "tcp-sendBuffer", server.getTcpSendBuffer()));
		server.setTcpReceiveBuffer(
				getElementAttribute2Int(namedNodeMap, "tcp-receiveBuffer", server.getTcpReceiveBuffer()));
		server.setTcpKeepAlive(getElementAttribute2Boolean(namedNodeMap, "tcp-keepAlive", server.isTcpKeepAlive()));
		server.setTcpNoDelay(getElementAttribute2Boolean(namedNodeMap, "tcp-noDelay", server.isTcpNoDelay()));
		server.setShutdownWhiteList(
				getElementAttribute(namedNodeMap, "shutdownWhiteList", server.getShutdownWhiteList()));
		server.setTcpShutdownPort(
				getElementAttribute2Int(namedNodeMap, "tcp-shutdownPort", server.getTcpShutdownPort()));
		server.setMaxThreads(getElementAttribute2Int(namedNodeMap, "maxThreads", server.getMaxThreads()));
		server.setQueueCapacity(getElementAttribute2Int(namedNodeMap, "queueCapacity", server.getQueueCapacity()));
		server.setKeepAliveMilliseconds(
				getElementAttribute2Long(namedNodeMap, "keepAliveMilliseconds", server.getKeepAliveMilliseconds()));
		server.setVirtualThread(getElementAttribute2Boolean(namedNodeMap, "virtualThread", server.isVirtualThread()));
	}

	public void loadGlobalAuth(Node globalAuthEle) {
		List<Node> globalUsers = getNodes(globalAuthEle.getChildNodes());
		Map<String, String> globalAuth = serverConfig.getGlobalAuth();
		for (Node globalUser : globalUsers) {
			NamedNodeMap namedNodeMap = globalUser.getAttributes();
			globalAuth.put(namedNodeMap.getNamedItem("username").getNodeValue(),
					namedNodeMap.getNamedItem("password").getNodeValue());
		}
	}

	public void loadModuleAuth(Node moduleAuthEle) {
		Map<String, Map<String, String>> moduleAuth = serverConfig.getModuleAuth();
		List<Node> modules = getNodes(moduleAuthEle.getChildNodes());
		for (Node module : modules) {
			Node nameNode = module.getAttributes().getNamedItem("name");
			if (nameNode != null) {
				String name = nameNode.getNodeValue();
				Map<String, String> usersMap = moduleAuth.get(name);
				if (usersMap == null) {
					usersMap = new HashMap<>();
					moduleAuth.put(name, usersMap);
				}
				List<Node> users = getNodes(module.getChildNodes());
				for (Node user : users) {
					NamedNodeMap namedNodeMap = user.getAttributes();
					usersMap.put(namedNodeMap.getNamedItem("username").getNodeValue(),
							namedNodeMap.getNamedItem("password").getNodeValue());
				}
			}
		}
	}

	public void loadModuleHealthCheck(Node healthCheckEle) {
		NamedNodeMap namedNodeMap = healthCheckEle.getAttributes();
		String check = namedNodeMap.getNamedItem("check").getNodeValue();
		if (check != null && check.trim().equals("on")) {
			HealthCheck healthCheck = serverConfig.getHealthCheck();
			healthCheck.setCheck(true);

			int port = Integer.parseInt(namedNodeMap.getNamedItem("port").getNodeValue());
			healthCheck.setPort(port);

			String scheme = namedNodeMap.getNamedItem("scheme").getNodeValue();
			healthCheck.setScheme(scheme);

			int backlog = Integer.parseInt(namedNodeMap.getNamedItem("backlog").getNodeValue());
			healthCheck.setBacklog(backlog);
			Node usernameNode = namedNodeMap.getNamedItem("username");
			if (usernameNode != null) {
				String username = usernameNode.getNodeValue();
				healthCheck.setUsername(username);
			}
			Node passwordNode = namedNodeMap.getNamedItem("password");
			if (passwordNode != null) {
				String password = passwordNode.getNodeValue();
				healthCheck.setPassword(password);
			}
			List<Node> componentElements = getNodes(healthCheckEle.getChildNodes());
			for (Node componentElement : componentElements) {
				Node checkNode = componentElement.getAttributes().getNamedItem("check");
				if (checkNode != null) {
					String componentCheck = checkNode.getNodeValue();
					if (componentCheck.trim().equals("on")) {
						healthCheck.addComponentCheck(componentElement.getNodeName());
					}
				}
			}
		}

	}

	public ServerConfigParser(URL binPath) throws Exception {
		this(binPath, null);
	}

	public ServerConfigParser(InputStream xmlInputStream) throws Exception {
		this(null, xmlInputStream);
	}

	public ServerConfigParser(URL binPath, InputStream xmlInputStream) throws Exception {
		serverConfig = new ServerConfig();
		serverConfig.setBinPath(binPath);
		InputStream xsdInputStream = getClass().getResourceAsStream("/server-conf.xsd");
		Document root;
		if (xmlInputStream != null) {
			root = new XmlObject(xmlInputStream, xsdInputStream).getDocument();
		} else {
			String xmlPath = binPath.getPath() + "../conf/server-conf.xml";
			root = new XmlObject(xmlPath, xsdInputStream).getDocument();
		}
		List<Node> childNodes = getNodes(root.getDocumentElement().getChildNodes());
		for (Node childNode : childNodes) {
			String childNodeName = childNode.getNodeName();
			if (childNodeName.equals("keyStore")) {
				loadKeyStore(childNode);
			} else if (childNodeName.equals("server")) {
				loadServer(childNode);
			} else if (childNodeName.equals("global-auth")) {
				loadGlobalAuth(childNode);
			} else if (childNodeName.equals("module-auth")) {
				loadModuleAuth(childNode);
			} else if (childNodeName.equals("health-check")) {
				loadModuleHealthCheck(childNode);
			}
		}

	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}
}
