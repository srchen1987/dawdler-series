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
package com.anywide.dawdler.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.cglib.proxy.Enhancer;
import com.anywide.dawdler.client.cglib.proxy.MethodInterceptor;
import com.anywide.dawdler.client.cglib.proxy.MethodProxy;
import com.anywide.dawdler.core.annotation.CircuitBreaker;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.annotation.RemoteServiceAssistant;
import com.anywide.dawdler.core.annotation.Service;
import com.anywide.dawdler.core.exception.NotSetRemoteServiceException;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceFactory.java
 * @Description 代理工厂，用于创建客户端代理对象，采用cglib
 * @date 2008年3月22日
 * @email suxuan696@gmail.com
 */
public class ServiceFactory {
	private static Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
	private static final ConcurrentHashMap<String, ConcurrentHashMap<Class<?>, Object>> PROXYOBJECTS = new ConcurrentHashMap<>();

	public static <T> T getService(final Class<T> delegate) {
		RemoteService remoteService = delegate.getAnnotation(RemoteService.class);
		if (remoteService == null) {
			throw new NotSetRemoteServiceException("not found @RemoteService on " + delegate.getName());
		}
		String groupName = remoteService.value();
		return getService(delegate, groupName, remoteService.loadBalance());
	}

	public static <T> T getService(Class<T> delegate, String groupName, String loadBalance,
			ClassLoader classLoader) {
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		ConcurrentHashMap<Class<?>, Object> proxy = PROXYOBJECTS.get(groupName);
		if (proxy == null) {
			proxy = new ConcurrentHashMap<>(32);
			ConcurrentHashMap<Class<?>, Object> preProxy = PROXYOBJECTS.putIfAbsent(groupName, proxy);
			if (preProxy != null) {
				proxy = preProxy;
			}
		}
		Object obj = proxy.get(delegate);
		if (obj == null) {
			obj = createCglibDynamicProxy(delegate, groupName, loadBalance, classLoader);
			Object preObj = proxy.putIfAbsent(delegate, obj);
			if (preObj != null) {
				obj = preObj;
			}

		}
		return (T) obj;
	}

	public static <T> T getService(Class<T> delegate, String groupName) {
		return getService(delegate, groupName, null, null);
	}

	public static <T> T getService(Class<T> delegate, String groupName, String loadBalance) {
		return getService(delegate, groupName, loadBalance, null);
	}

	private static <T> T createCglibDynamicProxy(Class<T> delegate, String groupName, String loadBalance,
			ClassLoader classLoader) {
		Enhancer enhancer = new Enhancer();
		enhancer.setCallback(new CglibInterceptor(delegate, groupName, loadBalance));
		enhancer.setInterfaces(new Class[] { delegate });
		enhancer.setClassLoader(classLoader);
		return (T) enhancer.create();
	}

	public static void injectRemoteService(Class<?> clazz, Object target, ClassLoader classLoader) {
		if (classLoader == null) {
			classLoader = clazz.getClassLoader();
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Service service = field.getAnnotation(Service.class);
			if (!field.getType().isPrimitive() && service != null) {
				Class<?> serviceClass = field.getType();
				field.setAccessible(true);
				RemoteService remoteService = serviceClass.getAnnotation(RemoteService.class);
				if (remoteService == null) {
					throw new NotSetRemoteServiceException("not found @RemoteService on " + serviceClass.getName());
				}
				String groupName = remoteService.value();
				try {
					field.set(target, getService(serviceClass, groupName, remoteService.loadBalance(), classLoader));
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

	private static class CglibInterceptor implements MethodInterceptor {
		private final String groupName;
		private final Class<?> delegate;
		private String serviceName;
		private boolean fuzzy;
		private int timeout;
		private String loadBalance;

		CglibInterceptor(Class<?> delegate, String groupName, String loadBalance) {
			this.groupName = groupName;
			getServiceName(delegate);
			this.delegate = delegate;
			this.loadBalance = loadBalance;
		}

		private void getServiceName(Class<?> delegate) {
			String serviceName = null;
			RemoteService rs = delegate.getAnnotation(RemoteService.class);
			if (rs != null) {
				serviceName = rs.serviceName();
				timeout = rs.timeout();
				fuzzy = rs.fuzzy();
			}
			if (serviceName == null || serviceName.trim().equals("")) {
				serviceName = delegate.getName();
			}
			this.serviceName = serviceName;
		}

		public Object intercept(Object object, Method method, Object[] objects, MethodProxy methodProxy)
				throws Throwable {
			boolean async = false;
			RemoteServiceAssistant remoteServiceAssistant = method.getAnnotation(RemoteServiceAssistant.class);
			if (remoteServiceAssistant != null) {
				fuzzy = remoteServiceAssistant.fuzzy();
				timeout = remoteServiceAssistant.timeout();
				loadBalance = remoteServiceAssistant.loadBalance();
				async = remoteServiceAssistant.async();
			}
			Transaction tr = TransactionProvider.getTransaction(groupName);
			tr.setMethod(method.getName());
			tr.setServiceName(serviceName);
			tr.setFuzzy(fuzzy);
			tr.setTimeout(timeout);
			tr.setCircuitBreaker(method.getAnnotation(CircuitBreaker.class));
			tr.setProxyInterface(delegate);
			tr.setLoadBalance(loadBalance);
			tr.setAsync(async);
			Class<?>[] types = method.getParameterTypes();
			for (int i = 0; i < types.length; i++) {
				Class<?> typeClass = types[i];
				tr.addObjectParam(typeClass, objects[i]);
			}
			return tr.executeResult();
		}
	}
}
