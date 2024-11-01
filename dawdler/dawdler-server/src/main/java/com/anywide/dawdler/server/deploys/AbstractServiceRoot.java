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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.anywide.dawdler.core.health.ServerHealth;
import com.anywide.dawdler.core.health.ServiceHealth;
import com.anywide.dawdler.core.health.Status;
import com.anywide.dawdler.core.httpserver.DawdlerHttpServer;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.thread.DefaultThreadFactory;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfig.HealthCheck;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.HashedWheelTimerSingleCreator;
import com.anywide.dawdler.util.JsonProcessUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * @author jackson.song
 * @version V1.0
 * deploy下服务模块的抽象根类
 */
public abstract class AbstractServiceRoot {
	protected Map<String, Service> servicesHealth;
	protected DawdlerHttpServer httpServer = null;
	protected static final Map<String, Service> SERVICES = new ConcurrentHashMap<>();
	protected ExecutorService dataProcessExecutor;

	protected String getProperty(String key) {
		return DawdlerTool.getProperty(key);
	}

	protected void initWorkPool(Server server) {
		int queueCapacity = server.getQueueCapacity();
		if (queueCapacity <= 0) {
			queueCapacity = Integer.MAX_VALUE;
		}
		long keepAliveMilliseconds = server.getKeepAliveMilliseconds();
		if (keepAliveMilliseconds < 0) {
			keepAliveMilliseconds = 0;
		}
		int nThreads = server.getMaxThreads();
		dataProcessExecutor = new ThreadPoolExecutor(nThreads, nThreads, keepAliveMilliseconds, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(queueCapacity), new DefaultThreadFactory("dataProcessWorkerPool#"));
	}

	public void shutdownWorkPool() {
		dataProcessExecutor.shutdown();
	}

	public void shutdownWorkPoolNow() {
		dataProcessExecutor.shutdownNow();
	}

	public void execute(Runnable runnable) {
		dataProcessExecutor.execute(runnable);
	}

	public abstract void initApplication(DawdlerServerContext dawdlerServerContext) throws Exception;

	public static Service getService(String path) {
		Service service = SERVICES.get(path);
		if (service == null) {
			return null;
		}
		Thread.currentThread().setContextClassLoader(service.getClassLoader());
		return service;
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
		httpServer.addPath(healthCheck.getUri() == null ? "/health" : healthCheck.getUri(), handler);
		httpServer.start();
	}

	public abstract void prepareDestroyedApplication();

	public abstract void destroyedApplication();

	public abstract void closeClassLoader();

	protected void releaseResource() {
		if (httpServer != null) {
			httpServer.stop();
		}
		SerializeDecider.destroyed();
		HashedWheelTimerSingleCreator.getHashedWheelTimer().stop();
	}

	public ServerHealth getServerHealth() {
		ClassLoader bootClassLoader = Thread.currentThread().getContextClassLoader();
		ServerHealth serverHealth = new ServerHealth();
		serverHealth.setStatus(Status.STARTING);
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

}
