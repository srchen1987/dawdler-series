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
import static com.anywide.dawdler.util.XmlObject.getElementAttribute2Long;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.conf.ServerConfig.HealthCheck;
import com.anywide.dawdler.server.conf.ServerConfig.KeyStore;
import com.anywide.dawdler.server.conf.ServerConfig.Scanner;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServerConfigParser.java
 * @Description 服务器配置解析类 (抛弃老版本的xstream实现，通过dom4j改造)
 * @date 2015年4月4日
 * @email suxuan696@gmail.com
 */
public class ServerConfigParser {
	private static final Logger logger = LoggerFactory.getLogger(ServerConfigParser.class);
	private ServerConfig serverConfig = new ServerConfig();

	public void loadJarFile(Element root) {
		List<Node> files = root.selectNodes("scanner/jar-files/jar-file");
		Scanner scanner = serverConfig.getScanner();
		for (Node node : files) {
			String jarFile = node.getText().trim();
			scanner.splitAndAddJarFiles(jarFile);
		}
	}

	public void loadPackagePath(Element root) {
		List<Node> files = root.selectNodes("scanner/packages-in-jar/package-path");
		Scanner scanner = serverConfig.getScanner();
		for (Node node : files) {
			String jarFile = node.getText().trim();
			scanner.splitAndAddPathInJar(jarFile);
		}
	}

	public void loadKeyStore(Element keyStoreEle) {
		String keyStorePath = getElementAttribute(keyStoreEle, "keyStorePath");
		String alias = getElementAttribute(keyStoreEle, "alias");
		String password = getElementAttribute(keyStoreEle, "password");
		KeyStore keyStore = serverConfig.getKeyStore();
		keyStore.setKeyStorePath(keyStorePath);
		keyStore.setAlias(alias);
		keyStore.setPassword(password);
	}

	public void loadServer(Element serverEle) {
		Server server = serverConfig.getServer();
		server.setHost(getElementAttribute(serverEle, "host", server.getHost()));
		server.setTcpPort(getElementAttribute2Int(serverEle, "tcp-port", server.getTcpPort()));
		server.setTcpBacklog(getElementAttribute2Int(serverEle, "tcp-backlog", server.getTcpBacklog()));
		server.setTcpSendBuffer(getElementAttribute2Int(serverEle, "tcp-sendBuffer", server.getTcpSendBuffer()));
		server.setTcpReceiveBuffer(
				getElementAttribute2Int(serverEle, "tcp-receiveBuffer", server.getTcpReceiveBuffer()));
		server.setTcpKeepAlive(getElementAttribute2Boolean(serverEle, "tcp-keepAlive", server.isTcpKeepAlive()));
		server.setTcpNoDelay(getElementAttribute2Boolean(serverEle, "tcp-noDelay", server.isTcpNoDelay()));
		server.setShutdownWhiteList(getElementAttribute(serverEle, "shutdownWhiteList", server.getShutdownWhiteList()));
		server.setTcpShutdownPort(getElementAttribute2Int(serverEle, "tcp-shutdownPort", server.getTcpShutdownPort()));
		server.setMaxThreads(getElementAttribute2Int(serverEle, "maxThreads", server.getMaxThreads()));
		server.setQueueCapacity(getElementAttribute2Int(serverEle, "queueCapacity", server.getQueueCapacity()));
		server.setKeepAliveMilliseconds(
				getElementAttribute2Long(serverEle, "keepAliveMilliseconds", server.getKeepAliveMilliseconds()));
	}

	public void loadGlobalAuth(Element globalAuthEle) {
		List<Node> globalUsers = globalAuthEle.selectNodes("user");
		Map<String, String> globalAuth = serverConfig.getGlobalAuth();
		for (Node globalUser : globalUsers) {
			Element globalUserEle = (Element) globalUser;
			globalAuth.put(globalUserEle.attributeValue("username", ""), globalUserEle.attributeValue("password", ""));
		}
	}

	public void loadModuleAuth(Element moduleAuthEle) {
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

	public void loadModuleHealthCheck(Element healthCheckEle) {
		if (healthCheckEle != null) {
			String check = healthCheckEle.attributeValue("check");
			if (check != null && check.trim().equals("on")) {
				HealthCheck healthCheck = serverConfig.getHealthCheck();
				healthCheck.setCheck(true);

				int port = Integer.parseInt(healthCheckEle.attributeValue("port"));
				healthCheck.setPort(port);

				String scheme = healthCheckEle.attributeValue("scheme");
				healthCheck.setScheme(scheme);

				int backlog = Integer.parseInt(healthCheckEle.attributeValue("port"));
				healthCheck.setBacklog(backlog);

				String username = healthCheckEle.attributeValue("username");
				healthCheck.setUsername(username);
				String password = healthCheckEle.attributeValue("password");
				healthCheck.setPassword(password);

				List<Element> componentElements = healthCheckEle.elements();
				for (Element componentElement : componentElements) {
					String componentCheck = componentElement.attributeValue("check");
					if (componentCheck != null && componentCheck.trim().equals("on")) {
						healthCheck.addComponentCheck(componentElement.getName());
					}
				}
			}
		}

	}

	public ServerConfigParser(URL binPath) throws Exception {
		serverConfig = new ServerConfig();
		serverConfig.setBinPath(binPath);
		try {
			XmlObject xmlo = new XmlObject(binPath.getPath() + "../conf/server-conf.xml");
			Element root = xmlo.getRoot();

			loadJarFile(root);

			loadPackagePath(root);

			Element keyStoreEle = (Element) root.selectSingleNode("keyStore");
			loadKeyStore(keyStoreEle);

			Element serverEle = (Element) root.selectSingleNode("server");
			loadServer(serverEle);

			Element globalAuthEle = (Element) root.selectSingleNode("global-auth");
			loadGlobalAuth(globalAuthEle);

			Element moduleAuthEle = (Element) root.selectSingleNode("module-auth");
			loadModuleAuth(moduleAuthEle);

			Element healthCheckEle = (Element) root.selectSingleNode("health-check");
			loadModuleHealthCheck(healthCheckEle);

		} catch (Exception e) {
			logger.error("", e);
			throw e;
		}
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}
}
