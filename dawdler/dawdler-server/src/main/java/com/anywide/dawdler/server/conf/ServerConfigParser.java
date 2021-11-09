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

import static com.anywide.dawdler.util.XmlObject.getElementAttribute;
import static com.anywide.dawdler.util.XmlObject.getElementAttribute2Boolean;
import static com.anywide.dawdler.util.XmlObject.getElementAttribute2Int;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.conf.ServerConfig.KeyStore;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;
/**
 * @author jackson.song
 * @version V1.0
 * @Title ServerConfigParser.java
 * @Description 服务器配置解析类 (抛弃老版本的xstream实现，通过dom4j改造)
 * @date 2015年4月04日
 * @email suxuan696@gmail.com
 */
public class ServerConfigParser {
	private static final Logger logger = LoggerFactory.getLogger(ServerConfigParser.class);
	private static ServerConfig serverConfig = new ServerConfig();

	static {
		try {
			XmlObject xmlo = new XmlObject(DawdlerTool.getcurrentPath() + "../conf/server-conf.xml");
			Element root = xmlo.getRoot();

			loadJarFile(root);

			Element keyStoreEle = (Element) root.selectSingleNode("keyStore");
			loadKeyStore(keyStoreEle);

			Element serverEle = (Element) root.selectSingleNode("server");
			loadServer(serverEle);

			Element globalAuthEle = (Element) root.selectSingleNode("global-auth");
			loadGlobalAuth(globalAuthEle);

			Element moduleAuthEle = (Element) root.selectSingleNode("module-auth");
			loadModuleAuth(moduleAuthEle);

		} catch (Exception e) {
			logger.error("", e);
		}

	}

	public static void loadJarFile(Element root) {
		List<Node> files = root.selectNodes("scanner/jarFile");
		Set<String> jarFiles = serverConfig.getScanner().getJarFiles();
		for (Node node : files) {
			String jarFile = node.getText().trim();
			jarFiles.add(jarFile);
		}
	}

	public static void loadKeyStore(Element keyStoreEle) {
		String keyStorePath = getElementAttribute(keyStoreEle, "keyStorePath");
		String alias = getElementAttribute(keyStoreEle, "alias");
		String password = getElementAttribute(keyStoreEle, "password");
		KeyStore keyStore = serverConfig.getKeyStore();
		keyStore.setKeyStorePath(keyStorePath);
		keyStore.setAlias(alias);
		keyStore.setPassword(password);
	}

	public static void loadServer(Element serverEle) {
		Server server = serverConfig.getServer();
		server.setHost(getElementAttribute(serverEle, "host", server.getHost()));
		server.setTcpPort(getElementAttribute2Int(serverEle, "tcpPort", server.getTcpPort()));
		server.setTcpBacklog(getElementAttribute2Int(serverEle, "tcpBacklog", server.getTcpBacklog()));
		server.setTcpSendBuffer(getElementAttribute2Int(serverEle, "tcpSendBuffer", server.getTcpSendBuffer()));
		server.setTcpReceiveBuffer(
				getElementAttribute2Int(serverEle, "tcpReceiveBuffer", server.getTcpReceiveBuffer()));
		server.setTcpKeepAlive(getElementAttribute2Boolean(serverEle, "tcpKeepAlive", server.isTcpKeepAlive()));
		server.setTcpNoDelay(getElementAttribute2Boolean(serverEle, "tcpNoDelay", server.isTcpNoDelay()));
		server.setShutdownWhiteList(getElementAttribute(serverEle, "shutdownWhiteList", server.getShutdownWhiteList()));
		server.setTcpShutdownPort(getElementAttribute2Int(serverEle, "tcpShutdownPort", server.getTcpShutdownPort()));
		server.setMaxThreads(getElementAttribute2Int(serverEle, "maxThreads", server.getMaxThreads()));
	}

	public static void loadGlobalAuth(Element globalAuthEle) {
		List<Node> globalUsers = globalAuthEle.selectNodes("user");
		Map<String, String> globalAuth = serverConfig.getGlobalAuth();
		for (Node globalUser : globalUsers) {
			Element globalUserEle = (Element) globalUser;
			globalAuth.put(globalUserEle.attributeValue("username", ""), globalUserEle.attributeValue("password", ""));
		}
	}

	public static void loadModuleAuth(Element moduleAuthEle) {
		Map<String, Map<String, String>> moduleAuth = serverConfig.getModuleAuth();
		List<Node> modules = moduleAuthEle.selectNodes("module");
		for (Node module : modules) {
			Element moduleEle = (Element) module;
			String name = moduleEle.attributeValue("name", "");
			if (!name.equals("")) {
				Map<String, String> usersMap = moduleAuth.get(name);
				if (usersMap == null) {
					usersMap = new HashMap<>();
					moduleAuth.put(name, usersMap);
				}
				List<Node> users = moduleEle.selectNodes("user");
				for (Node user : users) {
					Element userEle = (Element) user;
					usersMap.put(userEle.attributeValue("username", ""), userEle.attributeValue("password", ""));
				}
			}
		}
	}

	
	public static ServerConfig getServerConfig() {
		return serverConfig;
	}
}
