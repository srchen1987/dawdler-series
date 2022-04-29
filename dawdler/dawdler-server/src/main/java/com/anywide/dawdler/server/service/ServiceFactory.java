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
package com.anywide.dawdler.server.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceFactory.java
 * @Description 代理工厂，用于创建客户端代理对象，采用cglib 处理服务器端远程调用的过程
 * @date 2008年3月22日
 * @email suxuan696@gmail.com
 */
public class ServiceFactory {
	private static final ConcurrentHashMap<Class<?>, Object> proxyObjects = new ConcurrentHashMap<>();
	private static final Map<Class<?>, String> servicesName = new HashMap<>();

	public static <T> T getService(final Class<T> delegate, ServiceExecutor serviceExecutor,
			DawdlerContext dawdlerContext) {
		String serviceName = getServiceName(delegate);
		if (serviceName != null) {
			ServicesBean servicesBean = dawdlerContext.getServicesBean(serviceName);
			Object proxy = proxyObjects.get(delegate);
			if (proxy == null) {
				proxy = createCglibDynamicProxy(delegate, servicesBean, serviceExecutor);
				Object preProxy = proxyObjects.putIfAbsent(delegate, proxy);
				if (preProxy != null)
					proxy = preProxy;
			}
			return (T) proxy;
		}
		return null;
	}

	private static <T> T createCglibDynamicProxy(final Class<T> delegate, ServicesBean servicesBean,
			ServiceExecutor serviceExecutor) {
		Enhancer enhancer = new Enhancer();
		enhancer.setCallback(new CglibInterceptor(servicesBean, serviceExecutor));
		enhancer.setInterfaces(new Class[] { delegate });
		return (T) enhancer.create();
	}

	public static String getServiceName(Class<?> service) {
		String serviceName = servicesName.get(service);
		if (serviceName != null)
			return serviceName;
		RemoteService remoteService = service.getAnnotation(RemoteService.class);
		if (remoteService != null) {
			serviceName = remoteService.serviceName();
			if (serviceName.trim().equals("")) {
				serviceName = service.getName();
			}
			servicesName.put(service, serviceName);
			return serviceName;
		} else {
			Class<?>[] interfaceList = service.getInterfaces();
			for (Class<?> clazz : interfaceList) {
				remoteService = clazz.getAnnotation(RemoteService.class);
				if (remoteService == null) {
					continue;
				}
				serviceName = remoteService.serviceName();
				if (serviceName.trim().equals("")) {
					serviceName = service.getName();
				}
				servicesName.put(service, serviceName);
				return serviceName;
			}
			return null;
		}
	}

	private static class CglibInterceptor implements MethodInterceptor {
		private final ServicesBean servicesBean;
		private final ServiceExecutor serviceExecutor;

		public CglibInterceptor(ServicesBean servicesBean, ServiceExecutor serviceExecutor) {
			this.servicesBean = servicesBean;
			this.serviceExecutor = serviceExecutor;
		}

		public Object intercept(Object object, Method method, Object[] objects, MethodProxy methodProxy)
				throws Throwable {
			RequestBean requestBean = new RequestBean();
			requestBean.setArgs(objects);
			requestBean.setFuzzy(true);
			requestBean.setMethodName(method.getName());
			ResponseBean responseBean = new ResponseBean();
			serviceExecutor.execute(requestBean, responseBean, servicesBean);
			if (responseBean.getCause() != null)
				throw responseBean.getCause();
			return responseBean.getTarget();
		}
	}
}
