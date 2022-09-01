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
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.ServiceFactory;
import com.anywide.dawdler.clientplug.annotation.Controller;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.annotation.ResponseBody;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.handler.AnnotationUrlHandler;
import com.anywide.dawdler.clientplug.web.handler.RequestUrlData;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.interceptor.InterceptorProvider;
import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.clientplug.web.listener.WebContextListenerProvider;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.util.reflectasm.ParameterNameReader;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebComponentClassLoaderFire.java
 * @Description 客户端加载类通知类，初始化各种监听器 拦截器 controller 注入service
 * @date 2015年3月11日
 * @email suxuan696@gmail.com
 */
@Order(0)
public class WebComponentClassLoaderFire implements RemoteClassLoaderFire {
	private static final Logger logger = LoggerFactory.getLogger(WebComponentClassLoaderFire.class);

	@Override
	public void onLoadFire(Class<?> clazz, Object target, byte[] classCodes) throws Throwable {
		initListener(clazz, target);
		initInterceptor(clazz, target);
		initMapping(clazz, target, classCodes);
	}

	@Override
	public void onRemoveFire(Class<?> clazz) {
		removeMapping(clazz);
		InterceptorProvider.removeHandlerInterceptor(clazz);
		WebContextListenerProvider.removeWebContextListener(clazz);
	}

	private void initListener(Class<?> clazz, Object target) {
		if (WebContextListener.class.isAssignableFrom(clazz)) {
			try {
				WebContextListener listener = (WebContextListener) target;
				WebContextListenerProvider.addWebContextListener(listener);
				ServiceFactory.injectRemoteService(clazz, listener, clazz.getClassLoader());
				WebContextListenerProvider.order();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
	}

	private void initInterceptor(Class<?> clazz, Object target) {
		if (HandlerInterceptor.class.isAssignableFrom(clazz)) {
			HandlerInterceptor interceptor = (HandlerInterceptor) target;
			List<OrderData<HandlerInterceptor>> interceptors = InterceptorProvider.getHandlerInterceptors();
			for (OrderData<HandlerInterceptor> orderData : interceptors) {
				if (orderData.getData().getClass() == clazz) {
					return;
				}
			}
			InterceptorProvider.addHandlerInterceptor(interceptor);
			ServiceFactory.injectRemoteService(clazz, interceptor, clazz.getClassLoader());
			InterceptorProvider.order();
		}
	}

	private void initMapping(Class<?> clazz, Object target, byte[] classCodes) {
		if (clazz.isInterface()) {
			return;
		}
		if (clazz.getAnnotation(Controller.class) != null || TransactionController.class.isAssignableFrom(clazz)) {
			ServiceFactory.injectRemoteService(clazz, target, clazz.getClassLoader());
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

	public void registMapping(String prefix, Class<?> clazz, Object target) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			if (requestMapping != null && requestMapping.value().length > 0) {
				RequestUrlData requestUrlData = new RequestUrlData();
				method.setAccessible(true);
				requestUrlData.setMethod(method);
				requestUrlData.setRequestMapping(requestMapping);
				requestUrlData.setTarget(target);
				requestUrlData.setResponseBody(method.getAnnotation(ResponseBody.class));
				for (String requestMappingPath : requestMapping.value()) {
					try {
						String mapping = prefix == null ? requestMappingPath : (prefix + requestMappingPath);
						RequestUrlData preRequestUrlData = AnnotationUrlHandler.registMapping(mapping, requestUrlData);
						if (preRequestUrlData != null) {
							logger.error("regist {} failed because it was registered at {} {}", mapping,
									preRequestUrlData.getTarget().getClass().getName(), preRequestUrlData.getMethod());
						}
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		}
	}

	public void removeMapping(Class<?> clazz) {
		if (clazz.getAnnotation(Controller.class) != null || TransactionController.class.isAssignableFrom(clazz)) {
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
