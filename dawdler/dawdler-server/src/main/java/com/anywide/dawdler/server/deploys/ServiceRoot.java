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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.health.Status;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.server.deploys.loader.DeployClassLoader;
import com.anywide.dawdler.server.loader.DawdlerClassLoader;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceRoot.java
 * @Description deploy下服务模块的根实现(改造后继承AbstractServiceRoot)
 * @date 2015年3月22日
 * @email suxuan696@gmail.com
 */
public class ServiceRoot extends AbstractServiceRoot {
	private static final Logger logger = LoggerFactory.getLogger(ServiceRoot.class);
	public static final String DAWDLER_BASE_PATH = "DAWDLER_BASE_PATH";
	private static final String DAWDLER_DEPLOYS_PATH = "deploys";
	private static final String DAWDLER_LIB_PATH = "lib";

	public ClassLoader createServerClassLoader(URL binPath) {
		try {
			return DawdlerClassLoader.createLoader(binPath, Thread.currentThread().getContextClassLoader(),
					getLibURL());
		} catch (MalformedURLException e) {
			logger.error("", e);
			return null;
		}
	}

	private File getDeploys() {
		return new File(getProperty(DAWDLER_BASE_PATH), DAWDLER_DEPLOYS_PATH);
	}

	private URL[] getLibURL() throws MalformedURLException {
		return PathUtils.getRecursionLibURL(new File(getProperty(DAWDLER_BASE_PATH), DAWDLER_LIB_PATH));
	}

	public void initApplication(DawdlerServerContext dawdlerServerContext) throws Exception {
		ServerConfig serverConfig = dawdlerServerContext.getServerConfig();
		Server server = serverConfig.getServer();
		boolean healthCheck = serverConfig.getHealthCheck().isCheck();
		if (healthCheck) {
			servicesHealth = new ConcurrentHashMap<>(16);
		}
		initWorkPool(server);
		File deployFileRoot = getDeploys();
		File[] deployFiles = deployFileRoot.listFiles();
		if (deployFiles == null) {
			System.err.println(deployFileRoot.getAbsolutePath() + " not found, startup failed!");
			return;
		}
		long start = JVMTimeProvider.currentTimeMillis();
		if (deployFiles.length > 0) {
			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);
			ClassLoader classLoader = createServerClassLoader(serverConfig.getBinPath());
			List<DeployData> deployDataList = new ArrayList<>();
			for (File deployFile : deployFiles) {
				if (deployFile.isDirectory()) {
					String deployName = deployFile.getName();
					Callable<Void> call = (() -> {
						try {
							long serviceStart = JVMTimeProvider.currentTimeMillis();
							Service service = new ServiceBase(serverConfig, deployFile, classLoader,
									dawdlerServerContext.getStartSemaphore(), dawdlerServerContext.getStarted());
							SERVICES.put(deployName, service);
							if (healthCheck) {
								servicesHealth.put(deployName, service);
							}
							service.start();
							service.status(Status.UP);
							long serviceEnd = JVMTimeProvider.currentTimeMillis();
							System.out.println(deployName + " startup in " + (serviceEnd - serviceStart) + " ms!");
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
						return null;
					});
					Future<Void> future = executor.submit(call);
					deployDataList.add(new DeployData(deployName, future));
				}
			}
			for (DeployData deployData : deployDataList) {
				try {
					deployData.future.get();
				} catch (InterruptedException | ExecutionException e) {
					logger.error("", e);
					System.err.println(deployData.deployName + " startup failed!");
					Service service = SERVICES.remove(deployData.deployName);
					if (service != null) {
						service.status(Status.DOWN);
						service.cause(e);
						service.prepareStop();
						service.stop();
					}
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

	public void prepareDestroyedApplication() {
		SERVICES.values().forEach(Service::prepareStop);
	}

	public void destroyedApplication() {
		SERVICES.values().forEach(Service::stop);
		releaseResource();
	}

	public void closeClassLoader() {
		SERVICES.values().forEach(service -> {
			try {
				((DeployClassLoader) service.getClassLoader()).close();
			} catch (IOException e) {
			}
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
