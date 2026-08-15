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
import java.util.HashMap;
import java.util.Map;
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
		return getService(delegate, service.serviceName(), groupName, service.loadBalance());
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
		private final Class<?> delegate;
		private final String groupName;
		private final String serviceName;
		private final boolean fuzzy;
		private final int timeout;
		private final String loadBalance;
		private final Map<Method, MethodMetadata> methodCache;

		MethodInterceptor(Class<?> delegate, String serviceName, String groupName, String loadBalance) {
			this.delegate = delegate;
			this.groupName = groupName;
			this.loadBalance = loadBalance;
			boolean resolvedFuzzy = true;
			int resolvedTimeout = 120;
			String resolvedServiceName = serviceName;
			Service service = delegate.getAnnotation(Service.class);
			if (service != null) {
				if ("".equals(resolvedServiceName)) {
					resolvedServiceName = service.serviceName();
				}
				resolvedTimeout = service.timeout();
				resolvedFuzzy = service.fuzzy();
			}
			if ("".equals(resolvedServiceName)) {
				resolvedServiceName = delegate.getName();
			}
			this.serviceName = resolvedServiceName;
			this.timeout = resolvedTimeout;
			this.fuzzy = resolvedFuzzy;
			this.methodCache = buildMethodCache(delegate, resolvedFuzzy, resolvedTimeout, loadBalance);
		}

		private static Map<Method, MethodMetadata> buildMethodCache(Class<?> delegate, boolean defaultFuzzy,
				int defaultTimeout, String defaultLoadBalance) {
			Method[] methods = delegate.getMethods();
			Map<Method, MethodMetadata> cache = new HashMap<>((int) (methods.length / 0.75f) + 1);
			for (Method method : methods) {
				cache.put(method, resolveMetadata(method, defaultFuzzy, defaultTimeout, defaultLoadBalance));
			}
			return cache;
		}

		private static MethodMetadata resolveMetadata(Method method, boolean defaultFuzzy, int defaultTimeout,
				String defaultLoadBalance) {
			boolean fuzzy = defaultFuzzy;
			int timeout = defaultTimeout;
			String loadBalance = defaultLoadBalance;
			boolean async = false;
			RemoteServiceAssistant remoteServiceAssistant = method.getAnnotation(RemoteServiceAssistant.class);
			if (remoteServiceAssistant != null) {
				fuzzy = remoteServiceAssistant.fuzzy();
				timeout = remoteServiceAssistant.timeout();
				loadBalance = remoteServiceAssistant.loadBalance();
				async = remoteServiceAssistant.async();
			}
			CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
			Class<?>[] parameterTypes = method.getParameterTypes();
			return new MethodMetadata(fuzzy, timeout, loadBalance, async, circuitBreaker, parameterTypes);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			MethodMetadata metadata = methodCache.get(method);
			if (metadata == null) {
				metadata = resolveMetadata(method, fuzzy, timeout, loadBalance);
			}
			Transaction tr = TransactionProvider.getTransaction(groupName);
			tr.setMethod(method.getName());
			tr.setServiceName(serviceName);
			tr.setFuzzy(metadata.fuzzy);
			tr.setTimeout(metadata.timeout);
			tr.setCircuitBreaker(metadata.circuitBreaker);
			tr.setProxyInterface(delegate);
			tr.setLoadBalance(metadata.loadBalance);
			tr.setAsync(metadata.async);
			Class<?>[] types = metadata.parameterTypes;
			for (int i = 0; i < types.length; i++) {
				Class<?> typeClass = types[i];
				tr.addObjectParam(typeClass, args[i]);
			}
			return tr.executeResult();
		}
	}

	private static class MethodMetadata {
		final boolean fuzzy;
		final int timeout;
		final String loadBalance;
		final boolean async;
		final CircuitBreaker circuitBreaker;
		final Class<?>[] parameterTypes;

		MethodMetadata(boolean fuzzy, int timeout, String loadBalance, boolean async, CircuitBreaker circuitBreaker,
				Class<?>[] parameterTypes) {
			this.fuzzy = fuzzy;
			this.timeout = timeout;
			this.loadBalance = loadBalance;
			this.async = async;
			this.circuitBreaker = circuitBreaker;
			this.parameterTypes = parameterTypes;
		}
	}
}
