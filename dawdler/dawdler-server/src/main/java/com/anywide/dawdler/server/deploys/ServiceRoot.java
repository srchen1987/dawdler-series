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
package com.anywide.dawdler.server.deploys;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.health.ServerHealth;
import com.anywide.dawdler.core.health.ServiceHealth;
import com.anywide.dawdler.core.health.Status;
import com.anywide.dawdler.core.httpserver.DawdlerHttpServer;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.thread.DataProcessWorkerPool;
import com.anywide.dawdler.server.conf.DataSourceParser;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfig.HealthCheck;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.server.loader.DawdlerClassLoader;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.JVMTimeProvider;
import com.anywide.dawdler.util.JsonProcessUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceRoot.java
 * @Description deploy下服务模块的根实现
 * @date 2015年3月22日
 * @email suxuan696@gmail.com
 */
public class ServiceRoot {
	private static final Logger logger = LoggerFactory.getLogger(ServiceRoot.class);
	private static final Map<String, Service> services = new ConcurrentHashMap<>();
	private Map<String, Service> servicesHealth;
	public static final String DAWDLER_BASE_PATH = "DAWDLER_BASE_PATH";
	private static final String DAWDLER_DEPLOYS_PATH = "deploys";
	private static final String DAWDLER_LIB_PATH = "lib";
	private DataProcessWorkerPool dataProcessWorkerPool;
	private DawdlerHttpServer httpServer = null;

	public static Service getService(String path) {
		Service service = services.get(path);
		if (service == null) {
			return null;
		}
		Thread.currentThread().setContextClassLoader(service.getDawdlerContext().getClassLoader());
		return service;
	}

	public ClassLoader createServerClassLoader(URL binPath) {
		try {
			return DawdlerClassLoader.createLoader(binPath, Thread.currentThread().getContextClassLoader(),
					getLibURL());
		} catch (MalformedURLException e) {
			logger.error("", e);
			return null;
		}
	}

	private String getProperty(String key) {
		return DawdlerTool.getProperty(key);
	}

	private File getDeploys() {
		return new File(getProperty(DAWDLER_BASE_PATH), DAWDLER_DEPLOYS_PATH);
	}

	private URL[] getLibURL() throws MalformedURLException {
		return PathUtils.getRecursionLibURL(new File(getProperty(DAWDLER_BASE_PATH), DAWDLER_LIB_PATH));
	}

	private void initWorkPool(int nThreads, int queueCapacity, long keepAliveMilliseconds) {
		dataProcessWorkerPool = new DataProcessWorkerPool(nThreads, queueCapacity, keepAliveMilliseconds);
	}

	public void shutdownWorkPool() {
		dataProcessWorkerPool.shutdown();
	}

	public void shutdownWorkPoolNow() {
		dataProcessWorkerPool.shutdownNow();
	}

	public void execute(Runnable runnable) {
		dataProcessWorkerPool.execute(runnable);
	}

	public void initApplication(DawdlerServerContext dawdlerServerContext) throws Exception {
		ServerConfig serverConfig = dawdlerServerContext.getServerConfig();
		Server server = serverConfig.getServer();
		boolean healthCheck = serverConfig.getHealthCheck().isCheck();
		if (healthCheck) {
			servicesHealth = new ConcurrentHashMap<>(16);
		}
		initWorkPool(server.getMaxThreads(), server.getQueueCapacity(), server.getKeepAliveMilliseconds());
		File deployFileRoot = getDeploys();
		File[] deployFiles = deployFileRoot.listFiles();
		if (deployFiles == null) {
			System.err.println(deployFileRoot.getAbsolutePath() + " not found, startup failed!");
			return;
		}
		long start = JVMTimeProvider.currentTimeMillis();
		if (deployFiles.length > 0) {
			ExecutorService executor = Executors.newCachedThreadPool();
			ClassLoader classLoader = createServerClassLoader(serverConfig.getBinPath());
			if (classLoader != null) {
				try {
					DataSourceNamingInit.init(classLoader);
				} catch (ClassNotFoundException | NamingException | InstantiationException | IllegalAccessException e) {
					logger.error("", e);
				}
			}

			if (healthCheck) {
				validateDataSource();
			}
			List<DeployData> deployDataList = new ArrayList<>();
			for (File deployFile : deployFiles) {
				if (deployFile.isDirectory()) {
					String deployName = deployFile.getName();
					Callable<Void> call = (() -> {
						try {
							long serviceStart = JVMTimeProvider.currentTimeMillis();
							Service service = new ServiceBase(serverConfig, deployFile, classLoader);
							services.put(deployName, service);
							if (healthCheck) {
								servicesHealth.put(deployName, service);
							}
							service.start();
							service.status(Status.UP);
							long serviceEnd = JVMTimeProvider.currentTimeMillis();
							System.out.println(deployName + " startup in " + (serviceEnd - serviceStart) + " ms!");
						} catch (Throwable e) {
							logger.error(deployName, e);
							System.err.println(deployName + " startup failed!");
							Service service = services.remove(deployName);
							service.status(Status.DOWN);
							service.cause(e);
							service.prepareStop();
							service.stop();
						}
						return null;
					});
					Future<Void> future = executor.submit(call);
					deployDataList.add(new DeployData(deployName, future));
				}
			}
			for (DeployData deployData : deployDataList) {
				try {
					deployData.future.get(3, TimeUnit.MINUTES);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					System.out.println("server startup time out 3 minutes!");
					Service service = services.remove(deployData.deployName);
					service.prepareStop();
					service.stop();
				}
			}
			executor.shutdown();
		}
		long end = JVMTimeProvider.currentTimeMillis();
		System.out.println("Server startup in " + (end - start) + " ms,Listening port: "
				+ dawdlerServerContext.getServerConfig().getServer().getTcpPort() + "!");
		if (healthCheck) {
			startHttpServer(serverConfig);
		}
	}

	public void startHttpServer(ServerConfig serverConfig) throws Exception {
		HealthCheck healthCheck = serverConfig.getHealthCheck();
		String host = serverConfig.getServer().getHost();
		String scheme = healthCheck.getScheme();
		int port = healthCheck.getPort();
		int backlog = healthCheck.getBacklog();
		String username = healthCheck.getUsername();
		String password = healthCheck.getPassword();
		String keyStorePath = serverConfig.getKeyStore().getKeyStorePath();
		String keyPassword = serverConfig.getKeyStore().getPassword();
		httpServer = new DawdlerHttpServer(host, scheme, port, backlog, username, password, keyStorePath, keyPassword);
		HttpHandler handler = new HttpHandler() {
			@Override
			public void handle(HttpExchange exchange) throws IOException {
				ServerHealth serverHealth = getServerHealth();
				byte[] data = JsonProcessUtil.beanToJsonByte(serverHealth.getData());
				exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
				if (Status.UP.equals(serverHealth.getStatus())) {
					exchange.sendResponseHeaders(200, data.length);
				} else {
					exchange.sendResponseHeaders(500, data.length);
				}
				OutputStream out = exchange.getResponseBody();
				out.write(data);
				out.flush();
				out.close();
			}

		};
		httpServer.addPath("/status", handler);
		httpServer.start();
	}

	public void prepareDestroyedApplication() {
		services.values().forEach(v -> {
			v.prepareStop();
		});
	}

	public void destroyedApplication() {
		services.values().forEach(v -> {
			v.stop();
		});
		if (httpServer != null) {
			httpServer.stop();
		}
		SerializeDecider.destroyed();
		JVMTimeProvider.stop();
	}

	public static class DeployData {
		public DeployData(String deployName, Future<Void> future) {
			this.deployName = deployName;
			this.future = future;
		}

		private String deployName;
		private Future<Void> future;

	}

	public ServerHealth getServerHealth() {
		ClassLoader bootClassLoader = Thread.currentThread().getContextClassLoader();
		ServerHealth serverHealth = new ServerHealth();
		serverHealth.setStatus(Status.STARTING);
		try {
			validateDataSource();
		} catch (Exception e) {
			logger.error("", e);
			serverHealth.setStatus(Status.DOWN);
			serverHealth.setError(e.getMessage());
			return serverHealth;
		}
		Collection<Service> collection = servicesHealth.values();
		boolean down = false;
		for (Service service : collection) {
			ServiceHealth health = service.getServiceHealth();
			if (health.getStatus().equals(Status.DOWN)) {
				down = true;
			}
			serverHealth.addService(health);
		}
		serverHealth.setStatus(down ? Status.DOWN : Status.UP);
		Thread.currentThread().setContextClassLoader(bootClassLoader);
		return serverHealth;
	}

	private static boolean validateDataSource() throws Exception {
		Map<String, DataSource> datasources = DataSourceParser.getDataSources();
		if (datasources == null) {
			return true;
		}
		Set<Entry<String, DataSource>> entrySet = datasources.entrySet();
		for (Entry<String, DataSource> entry : entrySet) {
			String key = entry.getKey();
			DataSource dataSource = entry.getValue();
			Connection con = null;
			try {
				con = dataSource.getConnection();
				con.getAutoCommit();
			} catch (Exception e) {
				throw new Exception(key + ":" + e.getMessage());
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
					}
				}
			}
		}
		return true;
	}

}
