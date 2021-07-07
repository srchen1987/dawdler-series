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
package com.anywide.dawdler.clientplug.web.fire;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.ServiceFactory;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoderFire;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.bind.ParameterNameReader;
import com.anywide.dawdler.clientplug.web.handler.AnnotationUrlHandler;
import com.anywide.dawdler.clientplug.web.handler.RequestUrlData;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.interceptor.InterceptorProvider;
import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.clientplug.web.listener.WebContextListenerProvider;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebComponentClassLoaderFire.java
 * @Description 客户端加载类通知类，初始化各种监听器 拦截器 controller service等
 * @date 2015年03月11日
 * @email suxuan696@gmail.com
 */
@Order(0)
public class WebComponentClassLoaderFire implements RemoteClassLoderFire {
	private static final Logger logger = LoggerFactory.getLogger(WebComponentClassLoaderFire.class);

	@Override
	public void onLoadFire(Class<?> clazz,byte[] classCodes) {
		initListener(clazz);
		initInterceptor(clazz);
		initMapping(clazz, classCodes);
	}

	@Override
	public void onRemoveFire(Class<?> clazz) {
		removeMapping(clazz);
		InterceptorProvider.removeHandlerInterceptor(clazz);
		WebContextListenerProvider.removeWebContextListener(clazz);
	}

	private void initListener(Class<?> clazz) {
		if (WebContextListener.class.isAssignableFrom(clazz)) {
			try {
				WebContextListener listener = clazz.asSubclass(WebContextListener.class).newInstance();
				WebContextListenerProvider.addWebContextListener(listener);
				injectRemoteService(clazz, listener);
				WebContextListenerProvider.order();
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("", e);
			}
		}
	}

	private void initInterceptor(Class<?> clazz) {
		if (HandlerInterceptor.class.isAssignableFrom(clazz)) {
			try {
				HandlerInterceptor interceptor = (HandlerInterceptor) clazz.newInstance();
				List<OrderData<HandlerInterceptor>> interceptors = InterceptorProvider.getHandlerInterceptors();
				for (OrderData<HandlerInterceptor> orderData : interceptors) {
					if (orderData.getData().getClass() == clazz)
						return;
				}
				InterceptorProvider.addHandlerInterceptor(interceptor);
				injectRemoteService(clazz, interceptor);
				InterceptorProvider.order();
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("", e);
			}
		}
	}

	private void initMapping(Class<?> clazz, byte[] classCodes) {
		if (TransactionController.class.isAssignableFrom(clazz)) {
			TransactionController target;
			try {
				target = clazz.asSubclass(TransactionController.class).newInstance();
				injectRemoteService(clazz, target);
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("", e);
				return;
			}
			RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
			if (classRequestMapping != null && classRequestMapping.value().length > 0) {
				for (String classMapping : classRequestMapping.value()) {
					registMapping(classMapping, clazz, target);
				}
			} else {
				registMapping(null, clazz, target);
			}

			try {
				ParameterNameReader.loadAllDeclaredMethodsParameterNames(clazz, classCodes);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

	private void injectRemoteService(Class<?> clazz, Object target) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			RemoteService remoteService = field.getAnnotation(RemoteService.class);
			if (!field.getType().isPrimitive() && remoteService != null) {
				Class<?> serviceClass = field.getType();
				field.setAccessible(true);
				String groupName = remoteService.group();
				try {
					field.set(target, ServiceFactory.getService(serviceClass, groupName, remoteService.loadBalance()));
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

	public void registMapping(String prefix, Class<?> clazz, TransactionController target) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			if (requestMapping != null && requestMapping.value().length > 0) {
				RequestUrlData requestUrlData = new RequestUrlData();
				method.setAccessible(true);
				requestUrlData.setMethod(method);
				requestUrlData.setRequestMapping(requestMapping);
				requestUrlData.setTarget(target);
				for (String requestMappingPath : requestMapping.value()) {
					try {
						String mapping = prefix == null ? requestMappingPath : (prefix + requestMappingPath);
						RequestUrlData preRequestUrlData = AnnotationUrlHandler.registMapping(mapping, requestUrlData);
						if(preRequestUrlData != null) {
							logger.error("regist {} failed because it was registered at {} {}", mapping, preRequestUrlData.getTarget().getClass().getName(), preRequestUrlData.getMethod());
						}
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		}
	}

	public void removeMapping(Class<?> clazz) {
		if (TransactionController.class.isAssignableFrom(clazz)) {
			RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
			if (classRequestMapping != null && classRequestMapping.value().length > 0) {
				for (String classMapping : classRequestMapping.value()) {
					removeMapping(classMapping, clazz);
				}
			} else {
				removeMapping(null, clazz);
			}
			ParameterNameReader.removeParameterNames(clazz);
		}
	}

	private void removeMapping(String prefix, Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			if (requestMapping != null && requestMapping.value().length > 0) {
				for (String requestMappingPath : requestMapping.value()) {
					try {
						AnnotationUrlHandler
								.removeMapping(prefix == null ? requestMappingPath : (prefix + requestMappingPath));
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		}
	}
}
