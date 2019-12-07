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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateProvider;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;

/**
 * 
 * @Title: ServicesManager.java
 * @Description: 服务管理器
 * @author: jackson.song
 * @date: 2008年03月12日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ServicesManager {
	private static Logger logger = LoggerFactory.getLogger(ServicesManager.class);
	private DawdlerServiceCreateProvider dawdlerServiceCreateProvider = new DawdlerServiceCreateProvider();
	private ConcurrentHashMap<String, ServicesBean> services;

	public DawdlerServiceCreateProvider getDawdlerServiceCreateProvider() {
		return dawdlerServiceCreateProvider;
	}

	public ServicesManager() {
		services = new ConcurrentHashMap<String, ServicesBean>() {
			@Override
			public ServicesBean put(String key, ServicesBean value) {
				ServicesBean sb = super.putIfAbsent(key, value);
				if (sb != null) {
					logger.warn(key + " was registed at\t" + super.get(key));
					return sb;
				}
				return value;
			}
		};
	}

	public void fireCreate(DawdlerContext dawdlerContext) {
		services.forEach((k, v) -> {
			v.fireCreate(dawdlerContext);
		});
	}

	public ServicesBean getService(String name) {
		return this.services.get(name);
	}

	public void register(String name, Object service, boolean single) {
		ServicesBean serviceBean = createServicesBean(name, service, single);
		logger.info(name + "\t" + service);
		this.services.put(name, serviceBean);
	}

	public ServicesBean createServicesBean(String name, Object service, boolean single) {
		return new ServicesBean(name, service, dawdlerServiceCreateProvider, single);
	}

	public void registerService(Class<?> serviceInterface, Object service, boolean single) {
		logger.info(serviceInterface.getName() + "\t" + service);
		ServicesBean serviceBean = createServicesBean(serviceInterface.getName(), service, single);
		this.services.put(serviceBean.getName(), serviceBean);
	}

	public void clear() {
		services.clear();
	}

	public void smartRegister(Class<?> service)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (service == null) {
			throw new IllegalAccessException("service can't null!");
		}
		RemoteService remoteServiceContract = service.getAnnotation(RemoteService.class);
		if (remoteServiceContract != null) {
			String name = remoteServiceContract.value();
			if (StringUtils.isBlank(name)) {
				registerService(service, SunReflectionFactoryInstantiator.newInstance(service),
						remoteServiceContract.single());
			} else {
				register(name, SunReflectionFactoryInstantiator.newInstance(service), remoteServiceContract.single());
			}
		} else {
			Class<?>[] interfaceList = service.getInterfaces();
//			if(service.getSimpleName().equals("CheckUpdateImpl")) {
//				URL url = service.getResource(service.getSimpleName()+".class");
//				for (Class<?> clazz : interfaceList) {
//					remoteServiceContract = clazz.getAnnotation(RemoteService.class);
//				}
//			}
			if (interfaceList != null) {
				for (Class<?> clazz : interfaceList) {
					remoteServiceContract = clazz.getAnnotation(RemoteService.class);
					if (remoteServiceContract == null) {
						continue;
					}
					String name = remoteServiceContract.value();
					if (StringUtils.isBlank(name)) {
						registerService(clazz, SunReflectionFactoryInstantiator.newInstance(service),
								remoteServiceContract.single());
					} else {
						register(name, SunReflectionFactoryInstantiator.newInstance(service),
								remoteServiceContract.single());
					}
				}
			}
		}
	}

	public boolean isService(Class<?> service) {
		RemoteService remoteServiceContract = service.getAnnotation(RemoteService.class);
		if (remoteServiceContract != null) {
			return true;
		} else {
			Class<?>[] interfaceList = service.getInterfaces();
			if (interfaceList != null) {
				for (Class<?> clazz : interfaceList) {
					remoteServiceContract = clazz.getAnnotation(RemoteService.class);
					if (remoteServiceContract == null) {
						continue;
					}
					return true;
				}
			}
		}
		return false;
	}
}
