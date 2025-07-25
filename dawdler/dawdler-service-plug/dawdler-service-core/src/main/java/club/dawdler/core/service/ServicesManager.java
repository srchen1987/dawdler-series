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
package club.dawdler.core.service;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.service.annotation.Service;
import club.dawdler.core.service.bean.ServicesBean;
import club.dawdler.core.service.listener.DawdlerServiceCreateProvider;

/**
 * @author jackson.song
 * @version V1.0
 * 服务管理器
 */
public class ServicesManager {

	private static final Logger logger = LoggerFactory.getLogger(ServicesManager.class);
	private final DawdlerServiceCreateProvider dawdlerServiceCreateProvider = new DawdlerServiceCreateProvider();
	private final ConcurrentHashMap<String, ServicesBean> services;

	public ServicesManager() {
		services = new ConcurrentHashMap<String, ServicesBean>() {
			private static final long serialVersionUID = -8731173501243546754L;

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

	public void fireCreate() throws Throwable {
		for (Entry<String, ServicesBean> entry : services.entrySet()) {
			ServicesBean servicesBean = entry.getValue();
			if (servicesBean.isSingle()) {
				servicesBean.fireCreate();
			}

		}
	}

	public ServicesBean getService(String serviceName) {
		return this.services.get(serviceName);
	}

	public void register(String serviceName, Object service, boolean single) {
		ServicesBean serviceBean = createServicesBean(serviceName, service, single);
		this.services.put(serviceName, serviceBean);
	}

	public ServicesBean createServicesBean(String serviceName, Object service, boolean single) {
		return new ServicesBean(serviceName, service, dawdlerServiceCreateProvider, single);
	}

	public void registerService(Class<?> serviceInterface, Object service, boolean single) {
		ServicesBean serviceBean = createServicesBean(serviceInterface.getName(), service, single);
		this.services.put(serviceBean.getName(), serviceBean);
	}

	public void clear() {
		services.clear();
	}

	public void smartRegister(Class<?> type, Object target) throws IllegalArgumentException, SecurityException {
		Service service = type.getAnnotation(Service.class);
		if (service != null) {
			String serviceName = service.serviceName();
			if (serviceName.trim().equals("")) {
				if (type.isInterface()) {
					registerService(type, target, service.single());
				} else {
					Class<?>[] interfaceList = type.getInterfaces();
					for (Class<?> clazz : interfaceList) {
						Service interfaceService = clazz.getAnnotation(Service.class);
						if (interfaceService != null) {
							serviceName = interfaceService.serviceName();
						} else {
							serviceName = "";
						}
						if (serviceName.trim().equals("")) {
							registerService(clazz, target,
									interfaceService != null ? interfaceService.single() : service.single());
						} else {
							register(serviceName, target,
									interfaceService != null ? interfaceService.single() : service.single());
						}
					}
					Class<?> superClass = type.getSuperclass();
					if (superClass != null && superClass != Object.class) {
						Service superClassService = superClass.getAnnotation(Service.class);
						if (superClassService != null) {
							serviceName = superClassService.serviceName();
						} else {
							serviceName = "";
						}
						if (serviceName.trim().equals("")) {
							registerService(superClass, target,
									superClassService != null ? superClassService.single() : service.single());
						} else {
							register(serviceName, target,
									superClassService != null ? superClassService.single() : service.single());
						}
					}
				}
			} else {
				register(serviceName, target, service.single());
			}
		} else {
			Class<?>[] interfaceList = type.getInterfaces();
			for (Class<?> clazz : interfaceList) {
				service = clazz.getAnnotation(Service.class);
				if (service == null) {
					continue;
				}
				String serviceName = service.serviceName();
				if (serviceName.trim().equals("")) {
					registerService(clazz, target, service.single());
				} else {
					register(serviceName, target, service.single());
				}
			}
			Class<?> superClass = type.getSuperclass();
			if (superClass != null && superClass != Object.class) {
				service = superClass.getAnnotation(Service.class);
				if (service != null) {
					String serviceName = service.serviceName();
					if (serviceName.trim().equals("")) {
						registerService(superClass, target, service.single());
					} else {
						register(serviceName, target, service.single());
					}
				}
			}
		}
	}

	public boolean isService(Class<?> type) {
		Service service = type.getAnnotation(Service.class);
		if (service != null) {
			return true;
		} else {
			Class<?>[] interfaceList = type.getInterfaces();
			for (Class<?> clazz : interfaceList) {
				service = clazz.getAnnotation(Service.class);
				if (service == null) {
					continue;
				}
				return true;
			}
		}
		return false;
	}

}
