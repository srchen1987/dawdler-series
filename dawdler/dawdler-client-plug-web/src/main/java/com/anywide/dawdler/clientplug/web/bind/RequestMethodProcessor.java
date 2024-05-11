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
package com.anywide.dawdler.clientplug.web.bind;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;
import com.anywide.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import com.anywide.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RequestMethodProcessor.java
 * @Description 方法处理器,通过spi加载对应的组件
 * @date 2021年4月3日
 * @email suxuan696@gmail.com
 */
public class RequestMethodProcessor {
	private static List<MethodArgumentResolver> methodArgumentResolvers = new ArrayList<>();

	private static List<ParameterDiscoverer> parameterDiscoverers = new ArrayList<>();

	private static Map<Class<?>, Map<Method, List<RequestParamFieldData>>> requestParamFieldDataCache = new ConcurrentHashMap<>();

	private static Map<Class<?>, Map<RequestParamFieldData, MethodArgumentResolver>> paramFieldMethodArgumentResolverCache = new ConcurrentHashMap<>();
	static {
		ServiceLoader.load(MethodArgumentResolver.class).forEach(resolver -> {
			methodArgumentResolvers.add(resolver);
		});

		ServiceLoader.load(ParameterDiscoverer.class).forEach(discoverer -> {
			parameterDiscoverers.add(discoverer);
		});
	}

	public static Object[] process(Object controller, ViewForward viewForward, Method method) throws Exception {
		int parameterCount = method.getParameterCount();
		if (parameterCount == 0) {
			return null;
		}
		String uri = null;
		String antPath = viewForward.getAntPath();
		if (antPath != null) {
			uri = antPath;
		} else {
			uri = viewForward.getUriShort();
		}
		Class<?> controllerClass = controller.getClass();
		List<RequestParamFieldData> requestParamFieldDataList = loadRequestParamFieldDataList(controllerClass, method);
		Object[] args = new Object[parameterCount];
		for (RequestParamFieldData requestParamFieldData : requestParamFieldDataList) {
			MethodArgumentResolver methodArgumentResolver = matchResolver(controllerClass, requestParamFieldData);
			if (methodArgumentResolver != null) {
				args[requestParamFieldData.getIndex()] = methodArgumentResolver.resolveArgument(requestParamFieldData,
						viewForward, uri);
			}
		}
		return args;
	}

	private static List<RequestParamFieldData> loadRequestParamFieldDataList(Class<?> controllerClass, Method method) {
		Map<Method, List<RequestParamFieldData>> methodsCache = requestParamFieldDataCache.get(controllerClass);
		if (methodsCache == null) {
			methodsCache = new ConcurrentHashMap<>();
			Map<Method, List<RequestParamFieldData>> preMethodsCache = requestParamFieldDataCache
					.putIfAbsent(controllerClass, methodsCache);
			if (preMethodsCache != null) {
				methodsCache = preMethodsCache;
			}
		}
		List<RequestParamFieldData> requestParamFieldDataList = methodsCache.get(method);
		if (requestParamFieldDataList == null) {
			requestParamFieldDataList = new ArrayList<>();
			loadRequestParamFieldData(method, requestParamFieldDataList);
			List<RequestParamFieldData> preRequestParamFieldDataList = methodsCache.putIfAbsent(method,
					requestParamFieldDataList);
			if (preRequestParamFieldDataList != null) {
				requestParamFieldDataList = preRequestParamFieldDataList;
			}
		}
		return requestParamFieldDataList;
	}

	private static void loadRequestParamFieldData(Method method,
			List<RequestParamFieldData> requestParamFieldDataList) {
		String[] parameterNames = null;
		for (ParameterDiscoverer parameterDiscoverer : parameterDiscoverers) {
			parameterNames = parameterDiscoverer.getParameterNames(method);
			if (parameterNames != null) {
				break;
			}
		}
		if (parameterNames == null) {
			return;
		}
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			RequestParamFieldData requestParamFieldData = new RequestParamFieldData();
			requestParamFieldData.setAnnotations(parameter.getAnnotations());
			requestParamFieldData.setParamName(parameterNames[i]);
			requestParamFieldData.setType(parameter.getType());
			requestParamFieldData.setParameterType(method.getGenericParameterTypes()[i]);
			requestParamFieldData.setIndex(i);
			requestParamFieldDataList.add(requestParamFieldData);
		}
	}

	public static MethodArgumentResolver matchResolver(Class<?> controllerClass,
			RequestParamFieldData requestParamFieldData) {
		Map<RequestParamFieldData, MethodArgumentResolver> resolverCache = paramFieldMethodArgumentResolverCache
				.get(controllerClass);
		if (resolverCache == null) {
			resolverCache = new ConcurrentHashMap<>();
			Map<RequestParamFieldData, MethodArgumentResolver> preCache = paramFieldMethodArgumentResolverCache
					.putIfAbsent(controllerClass, resolverCache);
			if (preCache != null) {
				resolverCache = preCache;
			}
		}
		MethodArgumentResolver methodArgumentResolver = resolverCache.get(requestParamFieldData);
		if (methodArgumentResolver == null) {
			for (MethodArgumentResolver resolver : methodArgumentResolvers) {
				if (resolver.isSupport(requestParamFieldData)) {
					resolverCache.put(requestParamFieldData, resolver);
					return resolver;
				}
			}
		}
		return methodArgumentResolver;
	}

}
