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
package com.anywide.dawdler.server.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.anywide.dawdler.core.loader.DeployClassLoader;
import com.anywide.dawdler.core.service.ServicesManager;
import com.anywide.dawdler.core.service.context.ServiceContext;
import com.anywide.dawdler.server.conf.ServerConfig.HealthCheck;
import com.anywide.dawdler.server.service.conf.ServicesConfig;
import com.anywide.dawdler.server.service.conf.ServicesConfigParser;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.spring.antpath.AntPathMatcher;

/**
 * @author jackson.song
 * @version V1.0
 * dawdler上下文
 */
public class DawdlerContext extends ServiceContext {
	private final String deployName;
	private final String host;
	private final int port;
	private ServicesConfig servicesConfig;
	private AntPathMatcher antPathMatcher;
	private HealthCheck healthCheck;
	private Semaphore startSemaphore;
	private String serviceStatus;
	private AtomicBoolean started;

	public DawdlerContext(String deployName, String host, int port, ServicesManager servicesManager,
			AntPathMatcher antPathMatcher, HealthCheck healthCheck, Semaphore startSemaphore, AtomicBoolean started,
			String serviceStatus) {
		super(servicesManager);
		this.deployName = deployName;
		this.host = host;
		this.port = port;
		this.antPathMatcher = antPathMatcher;
		this.healthCheck = healthCheck;
		this.startSemaphore = startSemaphore;
		this.started = started;
		this.serviceStatus = serviceStatus;
	}

	public static DawdlerContext getDawdlerContext() {
		DawdlerContext context = (DawdlerContext) ((DeployClassLoader) Thread.currentThread().getContextClassLoader())
				.getDawdlerRuntimeContext();
		return context;
	}

	public String getDeployName() {
		return deployName;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public void initServicesConfig() throws Exception {
		String configPath;
		String activeProfile = System.getProperty("dawdler.profiles.active");
		String prefix = "services-conf";
		String suffix = ".xml";
		configPath = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + suffix;
		InputStream xmlInput = DawdlerTool.getResourceFromClassPath(prefix, suffix);
		if (xmlInput == null) {
			throw new IOException("not found " + configPath + " in classPath!");
		}
		InputStream xsdInput = getClass().getResourceAsStream("/services-conf.xsd");
		try {
			this.servicesConfig = new ServicesConfigParser(xmlInput, xsdInput).getServicesConfig();
		} finally {
			xmlInput.close();
		}
	}

	public ServicesConfig getServicesConfig() {
		return servicesConfig;
	}

	public AntPathMatcher getAntPathMatcher() {
		return antPathMatcher;
	}

	public HealthCheck getHealthCheck() {
		return healthCheck;
	}

	public void waitForStart() throws InterruptedException {
		if (!started.get()) {
			startSemaphore.acquire();
		}
	}

	public String getServiceStatus() {
		return serviceStatus;
	}

	public void setServiceStatus(String serviceStatus) {
		this.serviceStatus = serviceStatus;
	}

}
