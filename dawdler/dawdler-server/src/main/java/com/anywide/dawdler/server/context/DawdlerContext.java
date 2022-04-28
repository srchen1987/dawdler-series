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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.deploys.DawdlerDeployClassLoader;
import com.anywide.dawdler.server.deploys.ServiceBase;
import com.anywide.dawdler.server.service.ServiceFactory;
import com.anywide.dawdler.server.service.ServicesManager;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateProvider;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;
import com.anywide.dawdler.util.TLS;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.spring.antpath.AntPathMatcher;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerContext.java
 * @Description dawdler上下文
 * @date 2015年3月21日
 * @email suxuan696@gmail.com
 */
public class DawdlerContext {
	private static final String DAWDLER_CONTEXT_PREFIX = "dawdler_context_prefix";
	private final ClassLoader classLoader;
	private final String deployPath;
	private final String deployName;
	private final String deployClassPath;
	private final String host;
	private final int port;
	private final ServicesManager servicesManager;
	private final Map<Object, Object> attributes = new HashMap<>();
	private XmlObject servicesConfig;
	private AntPathMatcher antPathMatcher;
	
	public DawdlerContext(ClassLoader classLoader, String deployName, String deployPath, String deployClassPath,
			String host, int port, ServicesManager servicesManager, AntPathMatcher antPathMatcher) {
		this.classLoader = classLoader;
		this.deployPath = deployPath + File.separator;
		this.deployName = deployName;
		this.deployClassPath = deployClassPath + File.separator;
		this.host = host;
		this.port = port;
		this.servicesManager = servicesManager;
		this.antPathMatcher = antPathMatcher;
	}

	public static void remove() {
		TLS.remove(DAWDLER_CONTEXT_PREFIX);
	}

	public static DawdlerContext getDawdlerContext() {
		DawdlerContext context = (DawdlerContext) TLS.get(DAWDLER_CONTEXT_PREFIX);
		if (context == null)
			context = ((DawdlerDeployClassLoader) Thread.currentThread().getContextClassLoader()).getDawdlerContext();
		return context;
	}

	public static void setDawdlerContext(DawdlerContext dawdlerContext) {
		TLS.set(DAWDLER_CONTEXT_PREFIX, dawdlerContext);
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String getDeployPath() {
		return deployPath;
	}

	public String getDeployClassPath() {
		return deployClassPath;
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

	public Object getService(String name) throws Throwable {
		ServicesBean sb = getServicesBean(name);
		return sb == null ? null : sb.getService();
	}
	
	public ServicesBean getServicesBean(String name) {
		return servicesManager.getService(name);
	}

	public <T> T getService(Class<T> type) throws Throwable {
		String name = ServiceFactory.getServiceName(type);
		return (T) getService(name);
	}

	public <T> T getServiceProxy(Class<T> type) throws Throwable {
		Object obj = getAttribute(ServiceBase.SERVICE_EXECUTOR_PREFIX);
		if (obj != null)
			return ServiceFactory.getService(type, (ServiceExecutor) obj, this);
		return getService(type);
	}

	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	public Object getAttribute(Object key) {
		return attributes.get(key);
	}

	public void removeAttribute(Object key) {
		attributes.remove(key);
	}

	public DawdlerServiceCreateProvider getDawdlerServiceCreateProvider() {
		return servicesManager.getDawdlerServiceCreateProvider();
	}

	public void setServicesConfig(XmlObject servicesConfig) {
		this.servicesConfig = servicesConfig;
	}
	
	public XmlObject getServicesConfig() {
		return servicesConfig;
	}
	
	public AntPathMatcher getAntPathMatcher() {
		return antPathMatcher;
	}
}
