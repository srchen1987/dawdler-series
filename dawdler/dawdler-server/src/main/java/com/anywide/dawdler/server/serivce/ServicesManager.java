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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateProvider;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServicesManager.java
 * @Description 服务管理器
 * @date 2008年3月12日
 * @email suxuan696@gmail.com
 */
public class ServicesManager {
	private static final Logger logger = LoggerFactory.getLogger(ServicesManager.class);
	private final DawdlerServiceCreateProvider dawdlerServiceCreateProvider = new DawdlerServiceCreateProvider();
	private final ConcurrentHashMap<String, ServicesBean> services;

	public ServicesManager() {
		services = new ConcurrentHashMap<String, ServicesBean>() {
			@Override
			public ServicesBean put(String key, ServicesBean value) {
				ServicesBean sb = super.putIfAbsent(key, value);
				if (sb != null) {
					logger.warn(key + " was registered at\t" + super.get(key));
					return sb;
				}
				return value;
			}
		};
	}

	public DawdlerServiceCreateProvider getDawdlerServiceCreateProvider() {
		return dawdlerServiceCreateProvider;
	}

	public void fireCreate(DawdlerContext dawdlerContext) {
		services.forEach((k, v) -> {
			if (v.isSingle())
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

	public void smartRegister(Class<?> service) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (service == null) {
			throw new IllegalAccessException("service can't null!");
		}
		RemoteService remoteService = service.getAnnotation(RemoteService.class);
		if (remoteService != null) {
			String serviceName = remoteService.value();
			if (serviceName.trim().equals("")) {
				registerService(service, SunReflectionFactoryInstantiator.newInstance(service),
						remoteService.single());
			} else {
				register(serviceName, SunReflectionFactoryInstantiator.newInstance(service), remoteService.single());
			}
		} else {
			Class<?>[] interfaceList = service.getInterfaces();
			for (Class<?> clazz : interfaceList) {
				remoteService = clazz.getAnnotation(RemoteService.class);
				if (remoteService == null) {
					continue;
				}
				String serviceName = remoteService.value();
				if (serviceName.trim().equals("")) {
					registerService(clazz, SunReflectionFactoryInstantiator.newInstance(service),
							remoteService.single());
				} else {
					register(serviceName, SunReflectionFactoryInstantiator.newInstance(service),
							remoteService.single());
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
			for (Class<?> clazz : interfaceList) {
				remoteServiceContract = clazz.getAnnotation(RemoteService.class);
				if (remoteServiceContract == null) {
					continue;
				}
				return true;
			}
		}
		return false;
	}
}
