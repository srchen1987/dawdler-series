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
import com.anywide.dawdler.server.serivce.ServiceFactory;
import com.anywide.dawdler.server.serivce.ServicesManager;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateProvider;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;
import com.anywide.dawdler.util.TLS;

/**
 * 
 * @Title: DawdlerContext.java
 * @Description: dawdler上下文
 * @author: jackson.song
 * @date: 2015年03月21日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class DawdlerContext {
	private ClassLoader classLoader;// 类加载器
	private String deployPath;
	private String deployName;
	private String deployClassPath;
	private String host;
	private int port;
	private ServicesManager servicesManager;
	private Map<Object, Object> attributes = new HashMap<>();
	private static final String DAWDLERCONTEXT_PREFIX = "dawdler_context_prefix";

	public DawdlerContext(ClassLoader classLoader, String deployName, String deployPath, String deployClassPath,
			String host, int port, ServicesManager servicesManager) {
		this.classLoader = classLoader;
		this.deployPath = deployPath + File.separator;
		this.deployName = deployName;
		this.deployClassPath = deployClassPath + File.separator;
		this.host = host;
		this.port = port;
		this.servicesManager = servicesManager;
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

	public Object getService(String name) {
		ServicesBean sb = servicesManager.getService(name);
		return sb == null ? null : sb.getService();
	}

	public <T> T getService(Class<T> type) {
		String name = ServiceFactory.getServiceName(type);
		return (T) getService(name);
	}

	public <T> T getServiceProxy(Class<T> type) {
		Object obj = getAttribute(ServiceBase.SERVICEEXECUTOR_PREFIX);
		if (obj != null)
			return ServiceFactory.getService(type, (ServiceExecutor) obj, this);
		return getService(type);
	}

	public static void setDawdlerContext(DawdlerContext dawdlerContext) {
		TLS.set(DAWDLERCONTEXT_PREFIX, dawdlerContext);
	}

	public static void remove() {
		TLS.remove(DAWDLERCONTEXT_PREFIX);
	}

	public static DawdlerContext getDawdlerContext() {
		DawdlerContext context = (DawdlerContext) TLS.get(DAWDLERCONTEXT_PREFIX);
		if (context == null)
			context = ((DawdlerDeployClassLoader) Thread.currentThread().getContextClassLoader()).getDawdlerContext();
		return context;
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
}
