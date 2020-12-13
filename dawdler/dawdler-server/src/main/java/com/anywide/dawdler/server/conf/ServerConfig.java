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
import java.util.Map;
import java.util.Set;

import com.anywide.dawdler.server.deploys.ServiceRoot;
import com.anywide.dawdler.util.CertificateOperator;
import com.anywide.dawdler.util.CertificateOperator.KeyStoreConfig;
import com.anywide.dawdler.util.DawdlerTool;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 * @Title: ServerConfig.java
 * @Description: 服务器配置类
 * @author: jackson.song
 * @date: 2015年04月04日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
@XStreamAlias("conf")
public class ServerConfig {
	@XStreamAlias("server")
	private Server server;

	@XStreamAlias("keyStore")
	private KeyStore keyStore;

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	public Server getServer() {
		return server;
	}

	@XStreamAlias("scanner")
	private Scanner scanner;

	public Scanner getScanner() {
		return scanner;
	}

	@XStreamAlias("global-auth")
	private Map<String, String> globalAuth;

	public Map<String, String> getGlobalAuth() {
		return globalAuth;
	}

	public void setGlobalAuth(Map<String, String> globalAuth) {
		this.globalAuth = globalAuth;
	}

	@XStreamAlias("module-auth")
	private Map<String, Map<String, String>> moduleAuth;

	public Map<String, Map<String, String>> getModuleAuth() {
		return moduleAuth;
	}

	public void setModuleAuth(Map<String, Map<String, String>> moduleAuth) {
		this.moduleAuth = moduleAuth;
	}

	public class Scanner {
//		@XStreamAlias("file")
		@XStreamImplicit(itemFieldName = "file")
		private Set<String> file;

		public Set<String> getFile() {
			return file;
		}

		public void setFile(Set<String> file) {
			this.file = file;
		}
	}

	public class KeyStore {
		@XStreamAlias("keyStorePath")
		@XStreamAsAttribute
		private String keyStorePath;

		@XStreamAlias("alias")
		@XStreamAsAttribute
		private String alias;

		@XStreamAlias("password")
		@XStreamAsAttribute
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
//	host="0.0.0.0"	tcp-port="9527" tcp-backlog="200" tcp-sendBuffer="16384"
//				tcp-receiveBuffer="16384" tcp-keepAlive="true" tcp-noDelay="true">
		@XStreamAlias("host")
		@XStreamAsAttribute
		private String host;

		@XStreamAlias("tcp-port")
		@XStreamAsAttribute
		private int tcpPort = 9527;

		@XStreamAlias("tcp-backlog")
		@XStreamAsAttribute
		private int tcpBacklog = 200;

		@XStreamAlias("tcp-sendBuffer")
		@XStreamAsAttribute
		private int tcpSendBuffer = 16384;

		@XStreamAlias("tcp-receiveBuffer")
		@XStreamAsAttribute
		private int tcpReceiveBuffer = 16384;

		@XStreamAlias("tcp-keepAlive")
		@XStreamAsAttribute
		private boolean tcpKeepAlive = true;

		@XStreamAlias("tcp-noDelay")
		@XStreamAsAttribute
		private boolean tcpNoDelay = true;

		@XStreamAlias("tcp-shutdownPort")
		@XStreamAsAttribute
		private int tcpShutdownPort = 19527;

		@XStreamAlias("shutdownWhiteList")
		@XStreamAsAttribute
		private String shutdownWhiteList = "127.0.0.1,localhost";

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
	}

	private volatile CertificateOperator certificateOperator;

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
}
