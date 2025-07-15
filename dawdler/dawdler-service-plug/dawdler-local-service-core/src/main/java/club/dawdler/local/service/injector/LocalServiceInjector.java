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
package club.dawdler.local.service.injector;

import java.lang.reflect.Field;

import club.dawdler.core.service.annotation.Service;
import club.dawdler.core.service.bean.ServicesBean;
import club.dawdler.core.service.context.ServiceContext;
import club.dawdler.local.service.annotation.LocalService;

/**
 * @author jackson.song
 * @version V1.0
 * 注入本地服务通用类
 */
public class LocalServiceInjector {

	public static void injectLocalService(Object service, ServiceContext dawdlerContext) throws Throwable {
		Field[] fields = service.getClass().getDeclaredFields();
		for (Field field : fields) {
			LocalService localServiceAnnotation = field.getAnnotation(LocalService.class);
			if (field.getType().isPrimitive() || !field.getType().isInterface()) {
				continue;
			}
			Object obj = null;
			Class<?> serviceClass = field.getType();
			if (localServiceAnnotation != null) {
				String serviceName = localServiceAnnotation.serviceName();
				if ("".equals(serviceName) || serviceName == null) {
					serviceName = getServiceName(serviceClass);
				}
				ServicesBean servicesBean = dawdlerContext.getServicesBean(serviceName);
				if (servicesBean == null) {
					throw new ClassNotFoundException("not found " + serviceName + " !");
				}
				obj = servicesBean.getService();
			}
			if (obj != null) {
				field.setAccessible(true);
				field.set(service, obj);
			}
		}
	}

	public static String getServiceName(Class<?> type) {
		String serviceName = null;
		Service service = type.getAnnotation(Service.class);
		if (service != null) {
			serviceName = service.serviceName();
			if (serviceName.trim().equals("")) {
				serviceName = type.getName();
			}
			return serviceName;
		} else {
			Class<?>[] interfaceList = type.getInterfaces();
			for (Class<?> clazz : interfaceList) {
				service = clazz.getAnnotation(Service.class);
				if (service == null) {
					continue;
				}
				serviceName = service.serviceName();
				if (serviceName.equals("")) {
					serviceName = clazz.getName();
				}
				return serviceName;
			}
			Class<?> superClass = type.getSuperclass();
			if (superClass != null && superClass != Object.class) {
				Service superClassService = superClass.getAnnotation(Service.class);
				if (superClassService != null) {
					serviceName = superClassService.serviceName();
					if (serviceName.trim().equals("")) {
						serviceName = superClass.getName();
					}
					return serviceName;
				}
			}
			return type.getName();
		}
	}

}
