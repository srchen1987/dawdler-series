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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.core.annotation.ListenerConfig;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.filter.DawdlerFilter;
import com.anywide.dawdler.server.filter.FilterProvider;
import com.anywide.dawdler.server.listener.DawdlerListenerProvider;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;
import com.anywide.dawdler.server.serivce.ServiceFactory;
import com.anywide.dawdler.server.serivce.ServicesManager;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateListener;
import com.anywide.dawdler.server.thread.processor.DefaultServiceExecutor;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;

/**
 * 
 * @Title: ServiceBase.java
 * @Description: deploy下服务模块具体实现类
 * @author: jackson.song
 * @date: 2015年03月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ServiceBase implements Service {
	private static Logger logger = LoggerFactory.getLogger(ServiceBase.class);
	private static final String CLASSESPATH = "classes";
	private static final String LIBPATH = "lib";
	public static final String SERVICEEXECUTOR_PREFIX = "serviceExecutor_prefix";
	public static final String ASPECTSUPPORTOBJ = "aspectSupportObj";// aspect 支持
	public static final String ASPECTSUPPORTMETHOD = "aspectSupportMethod";
	private DawdlerDeployClassLoader classLoader;
	private File deploy;
	private DawdlerContext dawdlerContext;
	private ServiceExecutor defaultServiceExecutor = new DefaultServiceExecutor();
	private ServiceExecutor serviceExecutor = defaultServiceExecutor;
	private DawdlerListenerProvider dawdlerListenerProvider = new DawdlerListenerProvider();
	private ServicesManager servicesManager = new ServicesManager();
	private FilterProvider filterProvider = new FilterProvider();

	public ServiceBase(File deploy, String host, int port, ClassLoader parent) throws MalformedURLException {
		this.deploy = deploy;
		classLoader = DawdlerDeployClassLoader.createLoader(parent, getClassLoaderURL());
		dawdlerContext = new DawdlerContext(classLoader, deploy.getName(), deploy.getPath(), getClassesDir().getPath(),
				host, port, servicesManager);
		try {
			Class clazz = classLoader.loadClass("org.aspectj.weaver.loadtime.Aj");
			Object obj = clazz.newInstance();
			Method initializeMethod = clazz.getMethod("initialize");
			initializeMethod.invoke(obj);
			Method preProcessMethod = clazz.getMethod("preProcess", String.class, byte[].class, ClassLoader.class,
					ProtectionDomain.class);
			dawdlerContext.setAttribute(ASPECTSUPPORTMETHOD, preProcessMethod);
			dawdlerContext.setAttribute(ASPECTSUPPORTOBJ, obj);
		} catch (Exception e) {
			
		}
		classLoader.setDawdlerContext(dawdlerContext);
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	public ServicesBean getServiesBean(String name) {
		return servicesManager.getService(name);
	}

	public ServicesBean getServiesBeanNoSafe(String name) {
		return servicesManager.getService(name);
	}

	private File getClassesDir() {
		return new File(deploy, CLASSESPATH);
	}

	private URL[] getClassLoaderURL() throws MalformedURLException {
		File file = new File(deploy, LIBPATH);
		return PathUtils.getLibURL(file, getClassesDir().toURI().toURL());
	}

	public Class<?> getClass(String className) throws ClassNotFoundException {
		return classLoader.loadClass(className);
	}

	@Override
	public void start() throws Exception {
		try {
			Class<?> clazz = classLoader.loadClass("com.anywide.dawdler.serverplug.init.PlugInit");
			clazz.getConstructor(DawdlerContext.class).newInstance(dawdlerContext);
		} catch (Exception e) {
		}

		Object definedServiceExecutor = dawdlerContext.getAttribute(SERVICEEXECUTOR_PREFIX);
		Set<Class<?>> classes = null;
		if (definedServiceExecutor != null)
			serviceExecutor = (ServiceExecutor) definedServiceExecutor;
		classes = DeployClassesScanner.getClassesInPath(deploy);
		Set<Class<?>> serviceClasses = new HashSet<>();
		for (Class<?> c : classes) {
			if (((c.getModifiers() & 1024) != 1024) && ((c.getModifiers() & 16) != 16)
					&& ((c.getModifiers() & 16384) != 16384) && ((c.getModifiers() & 8192) != 8192)
					&& ((c.getModifiers() & 512) != 512)) {
				if (DawdlerServiceListener.class.isAssignableFrom(c)) {
					DawdlerServiceListener dl = (DawdlerServiceListener) SunReflectionFactoryInstantiator
							.newInstance(c);
					dawdlerListenerProvider.addHandlerInterceptors(dl);
				}
				if (DawdlerServiceCreateListener.class.isAssignableFrom(c)) {
					DawdlerServiceCreateListener dl = (DawdlerServiceCreateListener) SunReflectionFactoryInstantiator
							.newInstance(c);
					servicesManager.getDawdlerServiceCreateProvider().addHandlerInterceptors(dl);
				}
				if (DawdlerFilter.class.isAssignableFrom(c)) {
					Order order = c.getAnnotation(Order.class);
					DawdlerFilter filter = (DawdlerFilter) SunReflectionFactoryInstantiator.newInstance(c);
					OrderData<DawdlerFilter> orderData = new OrderData<>();
					orderData.setData(filter);
					if (order != null)
						orderData.setOrder(order.value());
					filterProvider.addFilter(filter);
				}

				if (servicesManager.isService(c)) {
					serviceClasses.add(c);
				}
			}
		}
		for (Class<?> c : serviceClasses) {
			servicesManager.smartRegister(c);
		}
		servicesManager.getDawdlerServiceCreateProvider().order();
		servicesManager.fireCreate(dawdlerContext);
		dawdlerListenerProvider.order();
		filterProvider.orderAndbuildChain();

		for (OrderData<DawdlerServiceListener> data : dawdlerListenerProvider.getListeners()) {
			injectService(data.getData());
		}

		for (OrderData<DawdlerFilter> data : filterProvider.getFilters()) {
			injectService(data.getData());
		}

		for (OrderData<DawdlerServiceListener> orderData : dawdlerListenerProvider.getListeners()) {
			ListenerConfig listenerConfig = orderData.getClass().getAnnotation(ListenerConfig.class);
			if (listenerConfig != null && listenerConfig.asyn()) {
				new Thread(() -> {
					if (listenerConfig.delayMsec() > 0) {
						try {
							Thread.sleep(listenerConfig.delayMsec());
						} catch (InterruptedException e) {
						}
						try {
							orderData.getData().contextInitialized(dawdlerContext);
						} catch (Exception e) {
							logger.error("", e);
						}
					}
				}).start();
			} else {
				orderData.getData().contextInitialized(dawdlerContext);
			}
		}
	}

	public FilterProvider getFilterProvider() {
		return filterProvider;
	}
	
	@Override
	public void prepareStop() {
		DiscoveryCenter discoveryCenter = (DiscoveryCenter) dawdlerContext.getAttribute(DiscoveryCenter.class);
		if(discoveryCenter != null) {
			try {
				discoveryCenter.destroy();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	@Override
	public void stop() {
		if (dawdlerListenerProvider.getListeners() != null) {
			for (int i = dawdlerListenerProvider.getListeners().size(); i > 0; i--) {
				try {
					dawdlerListenerProvider.getListeners().get(i - 1).getData().contextDestroyed(dawdlerContext);
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		servicesManager.clear();
	}

	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}

	@Override
	public ServiceExecutor getServiceExecutor() {
		return serviceExecutor;
	}

	private void injectService(Object service) {
		Field[] fields = service.getClass().getDeclaredFields();
		for (Field filed : fields) {
			RemoteService remoteService = filed.getAnnotation(RemoteService.class);
			if (!filed.getType().isPrimitive()) {
				Class<?> serviceClass = filed.getType();
				filed.setAccessible(true);
				try {
					Object obj = null;
					if (remoteService != null) {
						if (!remoteService.remote()) {
							obj = ServiceFactory.getService(serviceClass, serviceExecutor, dawdlerContext);
						} else {
							Class c = classLoader.loadClass("com.anywide.dawdler.client.ServiceFactory");
							Method method = c.getMethod("getService", Class.class, String.class);
							String groupName = remoteService.group();
							obj = method.invoke(null, serviceClass, groupName);
						}
						if (obj != null)
							filed.set(service, obj);
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

}
