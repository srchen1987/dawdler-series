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
package com.anywide.dawdler.remote.service.injector;

import java.lang.reflect.Field;

import com.anywide.dawdler.core.exception.NotSetRemoteServiceException;
import com.anywide.dawdler.core.service.annotation.Service;
import com.anywide.dawdler.remote.service.annotation.RemoteService;
import com.anywide.dawdler.remote.service.factory.ServiceFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RemoteServiceInjector.java
 * @Description 注入远程服务通用类
 * @date 2024年2月8日
 * @email suxuan696@gmail.com
 */
public class RemoteServiceInjector {

	public static void injectRemoteService(Object target) throws Throwable {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Field[] fields = target.getClass().getDeclaredFields();
		for (Field field : fields) {
			RemoteService remoteService = field.getAnnotation(RemoteService.class);
			if (field.getType().isPrimitive() || !field.getType().isInterface()) {
				continue;
			}
			Object obj = null;
			Class<?> serviceClass = field.getType();
			if (remoteService != null) {
				Service service = serviceClass.getAnnotation(Service.class);
				if (service == null) {
					throw new NotSetRemoteServiceException("not found @Service on " + serviceClass.getName());
				}
				String groupName = service.value();
				obj = ServiceFactory.getService(serviceClass, remoteService.serviceName(), groupName,
						service.loadBalance(), classLoader);
			}
			if (obj != null) {
				field.setAccessible(true);
				field.set(target, obj);
			}

		}
	}
}
