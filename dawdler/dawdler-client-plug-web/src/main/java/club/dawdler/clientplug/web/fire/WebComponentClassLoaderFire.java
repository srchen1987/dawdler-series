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
package club.dawdler.clientplug.web.fire;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.annotation.Controller;
import club.dawdler.clientplug.web.annotation.JsonIgnoreNull;
import club.dawdler.clientplug.web.annotation.RequestMapping;
import club.dawdler.clientplug.web.annotation.ResponseBody;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import club.dawdler.clientplug.web.handler.AnnotationUrlHandler;
import club.dawdler.clientplug.web.handler.RequestUrlData;
import club.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import club.dawdler.clientplug.web.interceptor.InterceptorProvider;
import club.dawdler.clientplug.web.listener.WebContextListener;
import club.dawdler.clientplug.web.listener.WebContextListenerProvider;
import club.dawdler.core.annotation.Order;
import club.dawdler.core.order.OrderData;
import club.dawdler.util.reflectasm.ParameterNameReader;

/**
 * @author jackson.song
 * @version V1.0
 * 客户端加载类通知类，初始化各种监听器 拦截器 controller 
 */
@Order(0)
public class WebComponentClassLoaderFire implements RemoteClassLoaderFire {
	private static final Logger logger = LoggerFactory.getLogger(WebComponentClassLoaderFire.class);

	@Override
	public void onLoadFire(Class<?> clazz, Object target) throws Throwable {
		initListener(clazz, target);
		initInterceptor(clazz, target);
		initMapping(clazz, target);
	}

	@Override
	public void onRemoveFire(Class<?> clazz) {
		removeMapping(clazz);
		InterceptorProvider.removeHandlerInterceptor(clazz);
		WebContextListenerProvider.removeWebContextListener(clazz);
	}

	private void initListener(Class<?> clazz, Object target) {
		if (WebContextListener.class.isAssignableFrom(clazz)) {
			WebContextListener listener = (WebContextListener) target;
			WebContextListenerProvider.addWebContextListener(listener);
			WebContextListenerProvider.order();
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
			InterceptorProvider.order();
		}
	}

	private void initMapping(Class<?> clazz, Object target) {
		if (clazz.isInterface()) {
			return;
		}
		if (clazz.getAnnotation(Controller.class) != null) {
			RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
			if (classRequestMapping != null && classRequestMapping.value().length > 0) {
				for (String classMapping : classRequestMapping.value()) {
					registMapping(classMapping, clazz, target);
				}
			} else {
				registMapping(null, clazz, target);
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
				requestUrlData.setJsonIgnoreNull(method.getAnnotation(JsonIgnoreNull.class));
				for (String requestMappingPath : requestMapping.value()) {
					String mapping = prefix == null ? requestMappingPath : (prefix + requestMappingPath);
					RequestUrlData preRequestUrlData = AnnotationUrlHandler.registMapping(mapping, requestUrlData);
					if (preRequestUrlData != null) {
						logger.error("regist {} failed because it was registered at {} {}", mapping,
								preRequestUrlData.getTarget().getClass().getName(), preRequestUrlData.getMethod());
					}
				}
			}
		}
	}

	public void removeMapping(Class<?> clazz) {
		if (clazz.getAnnotation(Controller.class) != null) {
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
					AnnotationUrlHandler
							.removeMapping(prefix == null ? requestMappingPath : (prefix + requestMappingPath));
				}
			}
		}
	}
}
