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
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.anywide.dawdler.server.deploys.ServiceRoot;
import com.anywide.dawdler.util.CertificateOperator;
import com.anywide.dawdler.util.CertificateOperator.KeyStoreConfig;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.spring.antpath.AntPathMatcher;
import com.anywide.dawdler.util.spring.antpath.StringUtils;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServerConfig.java
 * @Description 服务器配置类
 * @date 2015年4月4日
 * @email suxuan696@gmail.com
 */
public class ServerConfig {
	private URL binPath;
	private Server server;
	private KeyStore keyStore;
	private HealthCheck healthChecked = new HealthCheck();
	private Map<String, String> globalAuth = new HashMap<>();
	private Map<String, Map<String, String>> moduleAuth = new HashMap<>();
	private volatile CertificateOperator certificateOperator;
	private AntPathMatcher antPathMatcher = new AntPathMatcher();

	public ServerConfig() {
		this.keyStore = new KeyStore();
		this.server = new Server();
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	public Server getServer() {
		return server;
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
		Map<String, Map<String, String>> moduleAuth = getModuleAuth();
		boolean success = false;
		if (path != null) {
			Map<String, String> moduleAuths = moduleAuth.get(path);
			if (moduleAuths != null) {
				success = validate(moduleAuths, user, password);
			}
			if (!success) {
				success = validate(globalAuth, user, password);
			}
		}
		return success;
	}

	public boolean validate(Map<String, String> users, String user, String password) {
		boolean success = false;
		String passwd = users.get(user);
		if (passwd != null) {
			success = passwd.equals(password);
		}
		return success;
	}

	public AntPathMatcher getAntPathMatcher() {
		return antPathMatcher;
	}

	public class Scanner {
		private Set<String> jarFiles = new LinkedHashSet<String>();
		private Set<String> jarAntPathFiles = new LinkedHashSet<String>();

		private Set<String> packagePathInJar = new LinkedHashSet<String>();
		private Set<String> packageAntPathInJar = new LinkedHashSet<String>();

		public void splitAndAddJarFiles(String jarFilePath) {
			if (!StringUtils.hasLength(jarFilePath)) {
				return;
			}
			if (antPathMatcher.isPattern(jarFilePath)) {
				this.jarAntPathFiles.add(jarFilePath);
			} else {
				this.jarFiles.add(jarFilePath);
			}
		}

		public void splitAndAddPathInJar(String packagePath) {
			if (!StringUtils.hasLength(packagePath)) {
				return;
			}
			if (antPathMatcher.isPattern(packagePath)) {
				this.packageAntPathInJar.add(packagePath);
			} else {
				this.packagePathInJar.add(packagePath);
			}
		}

		public boolean matchInJars(String packagePath) {
			if (packagePathInJar.contains(packagePath)) {
				return true;
			}
			for (String antPath : packageAntPathInJar) {
				if (antPathMatcher.match(antPath, packagePath)) {
					return true;
				}
			}
			return false;
		}

		public boolean matchInJarFiles(String jarFilePath) {
			if (jarFiles.contains(jarFilePath)) {
				return true;
			}
			for (String antPath : jarAntPathFiles) {
				if (antPathMatcher.match(antPath, jarFilePath)) {
					return true;
				}
			}
			return false;
		}

		public boolean emptyJar() {
			return jarFiles.isEmpty() && jarAntPathFiles.isEmpty();
		}
	}

	public class KeyStore {
		private String keyStorePath;

		private String alias;

		private String password;

		public String getKeyStorePath() {
			if (keyStorePath != null) {
				keyStorePath = keyStorePath.replace("${" + ServiceRoot.DAWDLER_BASE_PATH + "}",
						DawdlerTool.getProperty(ServiceRoot.DAWDLER_BASE_PATH) + File.separator);
			}
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
		private int queueCapacity = 1024 * 64;
		private long keepAliveMilliseconds = 0;

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

		public int getQueueCapacity() {
			return queueCapacity;
		}

		public void setQueueCapacity(int queueCapacity) {
			this.queueCapacity = queueCapacity;
		}

		public long getKeepAliveMilliseconds() {
			return keepAliveMilliseconds;
		}

		public void setKeepAliveMilliseconds(long keepAliveMilliseconds) {
			this.keepAliveMilliseconds = keepAliveMilliseconds;
		}
	}

	public URL getBinPath() {
		return binPath;
	}

	public void setBinPath(URL binPath) {
		this.binPath = binPath;
	}

	public class HealthCheck {
		private boolean check;
		private int port;
		private String scheme;
		private int backlog;
		private String username;
		private String password;
		private Set<String> componentCheck;

		public HealthCheck() {
			componentCheck = new HashSet<>();
		}

		public boolean isCheck() {
			return check;
		}

		void setCheck(boolean check) {
			this.check = check;
		}

		void addComponentCheck(String componentName) {
			componentCheck.add(componentName);
		}

		public boolean componentCheck(String componentName) {
			return componentCheck.contains(componentName);
		}

		public int getPort() {
			return port;
		}

		void setPort(int port) {
			this.port = port;
		}

		public String getScheme() {
			return scheme;
		}

		void setScheme(String scheme) {
			this.scheme = scheme;
		}

		public int getBacklog() {
			return backlog;
		}

		void setBacklog(int backlog) {
			this.backlog = backlog;
		}

		public String getUsername() {
			return username;
		}

		void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		void setPassword(String password) {
			this.password = password;
		}

	}

	public HealthCheck getHealthCheck() {
		return healthChecked;
	}

}
