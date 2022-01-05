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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.thread.DataProcessWorkerPool;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfig.Scanner;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.server.loader.DawdlerClassLoader;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.JVMTimeProvider;

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
	public static final String DAWDLER_BASE_PATH = "DAWDLER_BASE_PATH";
	private static final String DAWDLER_DEPLOYS_PATH = "deploys";
	private static final String DAWDLER_LIB_PATH = "lib";
	private DataProcessWorkerPool dataProcessWorkerPool;

	public static Service getService(String path) {
		Service service = services.get(path);
		if (service == null)
			return null;
		Thread.currentThread().setContextClassLoader(service.getDawdlerContext().getClassLoader());
		DawdlerContext.setDawdlerContext(service.getDawdlerContext());
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

	public void initApplication(DawdlerServerContext dawdlerServerContext) {
		ServerConfig serverConfig = dawdlerServerContext.getServerConfig();
		Server server = serverConfig.getServer();
		Scanner scanner = serverConfig.getScanner();
		initWorkPool(server.getMaxThreads(), server.getQueueCapacity(), server.getKeepAliveMilliseconds());
		File deployFileRoot = getDeploys();
		File[] deployFiles = deployFileRoot.listFiles();
		if (deployFiles == null) {
			System.err.println("deploys not found, startup failed!");
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

			List<DeployData> deployDataList = new ArrayList<>();
			for (File deployFile : deployFiles) {
				if (deployFile.isDirectory()) {
					String deployName = deployFile.getName();
					Callable<Void> call = (() -> {
						try {
							long serviceStart = JVMTimeProvider.currentTimeMillis();
							Service service = new ServiceBase(serverConfig.getBinPath(), scanner,
									serverConfig.getAntPathMatcher(), deployFile, server.getHost(), server.getTcpPort(),
									classLoader);
							services.put(deployName, service);
							service.start();
							long serviceEnd = JVMTimeProvider.currentTimeMillis();
							System.out.println(deployName + " startup in " + (serviceEnd - serviceStart) + " ms!");
						} catch (Throwable e) {
							logger.error("", e);
							System.err.println(deployName + " startup failed!");
							Service service = services.remove(deployName);
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
	}

	public static class DeployData {
		public DeployData(String deployName, Future<Void> future) {
			this.deployName = deployName;
			this.future = future;
		}

		private String deployName;
		private Future<Void> future;

	}

}
