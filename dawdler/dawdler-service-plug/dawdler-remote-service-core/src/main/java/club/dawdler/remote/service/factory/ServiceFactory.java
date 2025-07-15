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
package club.dawdler.remote.service.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

import club.dawdler.client.Transaction;
import club.dawdler.client.TransactionProvider;
import club.dawdler.core.annotation.CircuitBreaker;
import club.dawdler.core.exception.NotSetRemoteServiceException;
import club.dawdler.core.service.annotation.Service;
import club.dawdler.remote.service.annotation.RemoteServiceAssistant;

/**
 * @author jackson.song
 * @version V1.0
 * 代理工厂，用于创建客户端代理对象，淘汰cglib改用jdk代理
 */
public class ServiceFactory {
	private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> PROXY_OBJECTS = new ConcurrentHashMap<>();

	public static <T> T getService(final Class<T> delegate) {
		Service service = delegate.getAnnotation(Service.class);
		if (service == null) {
			throw new NotSetRemoteServiceException("not found @Service on " + delegate.getName());
		}
		String groupName = service.value();
		return getService(delegate, groupName, service.loadBalance());
	}

	public static <T> T getService(Class<T> delegate, String serviceName, String groupName, String loadBalance, ClassLoader classLoader) {
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		ConcurrentHashMap<String, Object> proxy = PROXY_OBJECTS.get(groupName);
		if (proxy == null) {
			proxy = new ConcurrentHashMap<>(32);
			ConcurrentHashMap<String, Object> preProxy = PROXY_OBJECTS.putIfAbsent(groupName, proxy);
			if (preProxy != null) {
				proxy = preProxy;
			}
		}
		String cacheKey = delegate.getName()+serviceName;
		Object obj = proxy.get(cacheKey);
		if (obj == null) {
			obj = createDynamicProxy(delegate, serviceName, groupName, loadBalance, classLoader);
			Object preObj = proxy.putIfAbsent(cacheKey, obj);
			if (preObj != null) {
				obj = preObj;
			}
		}
		return (T) obj;
	}

	public static <T> T getService(Class<T> delegate, String serviceName, String groupName) {
		return getService(delegate, serviceName, groupName, null, null);
	}

	public static <T> T getService(Class<T> delegate, String serviceName, String groupName, String loadBalance) {
		return getService(delegate, serviceName, groupName, loadBalance, null);
	}

	private static <T> T createDynamicProxy(Class<T> delegate, String serviceName, String groupName, String loadBalance,
			ClassLoader classLoader) {
		return (T) Proxy.newProxyInstance(delegate.getClassLoader(), new Class[] { delegate },
				new MethodInterceptor(delegate, serviceName, groupName, loadBalance));
	}

	private static class MethodInterceptor implements InvocationHandler{
		private final String groupName;
		private final Class<?> delegate;
		private String serviceName;
		private boolean fuzzy;
		private int timeout;
		private String loadBalance;

		MethodInterceptor(Class<?> delegate, String serviceName, String groupName, String loadBalance) {
			this.groupName = groupName;
			this.serviceName = serviceName;
			getServiceName(delegate);
			this.delegate = delegate;
			this.loadBalance = loadBalance;
		}

		private void getServiceName(Class<?> delegate) {
			Service service = delegate.getAnnotation(Service.class);
			if (service != null) {
				if("".equals(serviceName)) {
					serviceName = service.serviceName();
				}
				timeout = service.timeout();
				fuzzy = service.fuzzy();
			}
			if ("".equals(serviceName)) {
				serviceName = delegate.getName();
			}
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
				tr.addObjectParam(typeClass, args[i]);
			}
			return tr.executeResult();
		}
	}
}
