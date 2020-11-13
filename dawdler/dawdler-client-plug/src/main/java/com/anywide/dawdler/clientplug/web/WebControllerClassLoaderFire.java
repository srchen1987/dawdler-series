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
package com.anywide.dawdler.clientplug.web;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.ServiceFactory;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoderFire;
import com.anywide.dawdler.clientplug.web.handler.AnnotationUrlHandler;
import com.anywide.dawdler.clientplug.web.handler.RequestUrlData;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.interceptor.InterceptorProvider;
import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.clientplug.web.listener.WebContextListenerProvider;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.order.OrderData;
/**
 * 
 * @Title:  WebControllerClassLoaderFire.java   
 * @Description:    客户端加载类通知类，初始化各种监听器 拦截器 controller service等   
 * @author: jackson.song    
 * @date:   2015年03月11日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class WebControllerClassLoaderFire implements RemoteClassLoderFire{
	private static Logger logger = LoggerFactory.getLogger(WebControllerClassLoaderFire.class);
	@Override
	public void onLoadFire(Class<?> clazz) {
		initListeners(clazz);
		initInterceptors(clazz);
		initMapping(clazz);
	}

	@Override
	public void onRemoveFire(Class<?> clazz) {
		removeMapping(clazz);
	}
	private void initListeners(Class<?> clazz) {
		if(WebContextListener.class.isAssignableFrom(clazz)) {
			try {
				WebContextListener listener = (WebContextListener)clazz.newInstance();
				WebContextListenerProvider.addWebContextListeners(listener);
				injectRemoteService(clazz,listener);
				WebContextListenerProvider.order();
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("",e);
			}
		}
	}
	private void initInterceptors(Class<?> clazz) {
		if(HandlerInterceptor.class.isAssignableFrom(clazz)) {
			try {
				HandlerInterceptor interceptor =(HandlerInterceptor) clazz.newInstance();
				List<OrderData<HandlerInterceptor>> list = InterceptorProvider.getHandlerInterceptors();
				for(OrderData<HandlerInterceptor> orderData : list) {
					if(orderData.getData().getClass() == clazz)
						return;
				}
				InterceptorProvider.addHandlerInterceptors(interceptor);
				injectRemoteService(clazz, interceptor);
				InterceptorProvider.order();
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("",e);
			}
		}
	}
	private void initMapping(Class<?> clazz) {
		if(TransactionController.class.isAssignableFrom(clazz)) {
			TransactionController target;
			try {
				target = clazz.asSubclass(TransactionController.class).newInstance();
				injectRemoteService(clazz, target);
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("",e);
				return;
			}
			RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
			if(classRequestMapping!=null&&classRequestMapping.value().length>0){
				for(String classMapping:classRequestMapping.value()) {
					registMapping(classMapping, clazz, target);
				}
			}else {
				registMapping(null, clazz, target);
			}
		}
	}
	private void injectRemoteService(Class<?> clazz,Object target) {
		Field[] fields =  clazz.getDeclaredFields();
		for(Field filed : fields) {
			RemoteService rs = filed.getAnnotation(RemoteService.class);
			if(!filed.getType().isPrimitive()&&rs!=null) {
				Class<?> serviceClass = filed.getType();
				filed.setAccessible(true);
				String groupName = rs.group();
				try {
					filed.set(target, ServiceFactory.getService(serviceClass, groupName));
				} catch (Exception e) {
					logger.error("",e);
				}
			}
		}
	}
	public void registMapping(String prefix,Class<?> clazz,TransactionController target) {
		Method[] methods = clazz.getMethods();
		for(Method method : methods) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			if(requestMapping!=null&&requestMapping.value().length>0) {
				RequestUrlData rd = new RequestUrlData();
				method.setAccessible(true);
				rd.setMethod(method);
				rd.setRequestMapping(requestMapping);
				rd.setTarget(target);
				for(String requestMappingPath:requestMapping.value()) {
					try { 
						AnnotationUrlHandler.registMapping(prefix==null?requestMappingPath:(prefix+requestMappingPath),rd);
					} catch (Exception e) {
						logger.error("",e);
					}
				}
			}
		}
	}
	public void removeMapping(Class<?> clazz) {
		if(clazz.isAssignableFrom(TransactionController.class)) {
			RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
			if(classRequestMapping!=null&&classRequestMapping.value().length>0){
				for(String classMapping:classRequestMapping.value()) {
					removeMapping(classMapping, clazz);
				}
			}else {
				removeMapping(null, clazz);
			}
		}
	}
	private void removeMapping(String prefix,Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		for(Method method : methods) {
			RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
			if(requestMapping!=null&&requestMapping.value().length>0) {
				for(String requestMappingPath:requestMapping.value()) {
					try {
						AnnotationUrlHandler.removeMapping(prefix==null?requestMappingPath:(prefix+requestMappingPath));
					} catch (Exception e) {
						logger.error("",e);
					}
				}
			}
		}
	}
}
