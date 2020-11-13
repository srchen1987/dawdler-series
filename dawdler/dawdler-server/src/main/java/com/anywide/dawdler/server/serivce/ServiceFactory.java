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
package com.anywide.dawdler.server.serivce;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

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
 * 
 * @Title: ServiceFactory.java
 * @Description: 代理工厂，用于创建客户端代理对象，采用cglib 处理服务器端远程调用的过程
 * @author: jackson.song
 * @date: 2008年03月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ServiceFactory {
	private static ConcurrentHashMap<Class<?>, Object> proxyObjects = new ConcurrentHashMap<>();

	public static <T> T getService(final Class<T> delegate, ServiceExecutor serviceExecutor,
			DawdlerContext dawdlerContext) {
		String name = getServiceName(delegate);
		if (name != null) {
			ServicesBean servicesBean = new ServicesBean(name, dawdlerContext.getService(name),
					dawdlerContext.getDawdlerServiceCreateProvider());
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
		T cglibProxy = (T) enhancer.create();
		return cglibProxy;
	}

	private static class CglibInterceptor implements MethodInterceptor {
		private ServicesBean servicesBean;
		private ServiceExecutor serviceExecutor;

		public CglibInterceptor(ServicesBean servicesBean, ServiceExecutor serviceExecutor) {
			this.servicesBean = servicesBean;
			this.serviceExecutor = serviceExecutor;
		}

		public Object intercept(Object object, Method method, Object[] objects, MethodProxy methodProxy)
				throws Throwable {
			RequestBean resquestBean = new RequestBean();
			resquestBean.setArgs(objects);
			resquestBean.setFuzzy(true);
			resquestBean.setMethodName(method.getName());
			ResponseBean responseBean = new ResponseBean();
			serviceExecutor.execute(resquestBean, responseBean, servicesBean);
			if (responseBean.getCause() != null)
				throw responseBean.getCause();
			return responseBean.getTarget();
		}
	}
	private static Map<Class,String> servicesName = new HashMap<>();
	public static String getServiceName(Class<?> service) {
		String name = servicesName.get(service);
		if(name!=null)return name;
		RemoteService remoteServiceContract = service.getAnnotation(RemoteService.class);
		if(remoteServiceContract!=null){
			 name = remoteServiceContract.value();
			if (StringUtils.isBlank(name)) {
				name = service.getName();
			} 
			servicesName.put(service, name);
			return name;
		}else{
			Class<?>[] interfaceList = service.getInterfaces();
			if (interfaceList != null) {
				for (Class<?> clazz : interfaceList) {
					remoteServiceContract = clazz.getAnnotation(RemoteService.class);
					if (remoteServiceContract == null) {
						continue;
					}
					name = remoteServiceContract.value();
					if (StringUtils.isBlank(name)) {
						name =  service.getName();
					}
					servicesName.put(service, name);
					return name;
				}
			}
			return null;
		}
	}
}
