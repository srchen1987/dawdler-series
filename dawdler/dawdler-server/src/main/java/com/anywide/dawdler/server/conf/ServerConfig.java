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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.anywide.dawdler.server.deploys.ServiceRoot;
import com.anywide.dawdler.util.CertificateOperator;
import com.anywide.dawdler.util.CertificateOperator.KeyStoreConfig;
import com.anywide.dawdler.util.DawdlerTool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServerConfig.java
 * @Description 服务器配置类
 * @date 2015年4月04日
 * @email suxuan696@gmail.com
 */
public class ServerConfig {
	public ServerConfig() {
		this.scanner = new Scanner();
		this.keyStore = new KeyStore();
		this.server = new Server();
	}

	private Server server;

	private KeyStore keyStore;
	private Scanner scanner;
	private Map<String, String> globalAuth = new HashMap<>();
	private Map<String, Map<String, String>> moduleAuth = new HashMap<>();
	private volatile CertificateOperator certificateOperator;

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	public Server getServer() {
		return server;
	}

	public Scanner getScanner() {
		return scanner;
	}

	public Map<String, String> getGlobalAuth() {
		return globalAuth;
	}

	public Map<String, Map<String, String>> getModuleAuth() {
		return moduleAuth;
	}

	public boolean auth(String path, String user, byte[] passwordByte) throws Exception {
		if (certificateOperator == null) {
			synchronized (this) {
				if (certificateOperator == null) {
					KeyStore keyStore = getKeyStore();
					certificateOperator = new CertificateOperator(keyStore.getKeyStorePath(), keyStore.getAlias(),
							keyStore.getPassword());
				}
			}
		}
		passwordByte = certificateOperator.decrypt(passwordByte, KeyStoreConfig.DKS);
		String password = new String(passwordByte);
		Map<String, String> globalAuth = getGlobalAuth();
		Map<String, Map<String, String>> moduleAuth = getModuleAuth();
		boolean success = false;
		if (path != null) {
			Map<String, String> moduleAuths = moduleAuth.get(path);
			if (moduleAuths != null) {
				success = validate(moduleAuths, user, password);
			}
			if (!success)
				success = validate(globalAuth, user, password);
		}
		return success;
	}

	public boolean validate(Map<String, String> users, String user, String password) {
		boolean success = false;
		String passwd = globalAuth.get(user);
		if (passwd != null) {
			success = passwd.equals(password);
		}
		return success;
	}

	public class Scanner {
		private Set<String> jarFiles = new LinkedHashSet<String>();

		public Set<String> getJarFiles() {
			return jarFiles;
		}

	}

	public class KeyStore {
		private String keyStorePath;

		private String alias;

		private String password;

		public String getKeyStorePath() {
			if (keyStorePath != null)
				keyStorePath = keyStorePath.replace("${DAWDLER_BASE_PATH}",
						DawdlerTool.getEnv(ServiceRoot.DAWDLER_BASE_PATH) + File.separator);
			return keyStorePath;
		}

		public void setKeyStorePath(String keyStorePath) {
			this.keyStorePath = keyStorePath;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

	public class Server {
		private String host = "0.0.0.0";
		private int tcpPort = 9527;
		private int tcpBacklog = 200;
		private int tcpSendBuffer = 16384;
		private int tcpReceiveBuffer = 16384;
		private boolean tcpKeepAlive = true;
		private boolean tcpNoDelay = true;
		private int tcpShutdownPort = 19527;
		private String shutdownWhiteList = "127.0.0.1,localhost";
		private int maxThreads = 200;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getTcpShutdownPort() {
			return tcpShutdownPort;
		}

		public void setTcpShutdownPort(int tcpShutdownPort) {
			this.tcpShutdownPort = tcpShutdownPort;
		}

		public String getShutdownWhiteList() {
			return shutdownWhiteList;
		}

		public void setShutdownWhiteList(String shutdownWhiteList) {
			this.shutdownWhiteList = shutdownWhiteList;
		}

		public int getTcpPort() {
			return tcpPort;
		}

		public void setTcpPort(int tcpPort) {
			this.tcpPort = tcpPort;
		}

		public int getTcpBacklog() {
			return tcpBacklog;
		}

		public void setTcpBacklog(int tcpBacklog) {
			this.tcpBacklog = tcpBacklog;
		}

		public int getTcpSendBuffer() {
			return tcpSendBuffer;
		}

		public void setTcpSendBuffer(int tcpSendBuffer) {
			this.tcpSendBuffer = tcpSendBuffer;
		}

		public int getTcpReceiveBuffer() {
			return tcpReceiveBuffer;
		}

		public void setTcpReceiveBuffer(int tcpReceiveBuffer) {
			this.tcpReceiveBuffer = tcpReceiveBuffer;
		}

		public boolean isTcpKeepAlive() {
			return tcpKeepAlive;
		}

		public void setTcpKeepAlive(boolean tcpKeepAlive) {
			this.tcpKeepAlive = tcpKeepAlive;
		}

		public boolean isTcpNoDelay() {
			return tcpNoDelay;
		}

		public void setTcpNoDelay(boolean tcpNoDelay) {
			this.tcpNoDelay = tcpNoDelay;
		}

		public int getMaxThreads() {
			return maxThreads;
		}

		public void setMaxThreads(int maxThreads) {
			this.maxThreads = maxThreads;
		}
	}
}
