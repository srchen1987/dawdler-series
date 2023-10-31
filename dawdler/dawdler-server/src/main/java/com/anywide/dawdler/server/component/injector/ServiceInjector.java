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
package com.anywide.dawdler.server.component.injector;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.ServiceBase;
import com.anywide.dawdler.server.service.ServicesManager;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceInjector.java
 * @Description 注入服务
 * @date 2023年7月20日
 * @email suxuan696@gmail.com
 */
@Order(1)
public class ServiceInjector implements CustomComponentInjector {

	@Override
	public void inject(Class<?> type, Object target) throws Throwable {
		ServicesManager servicesManager = (ServicesManager) DawdlerContext.getDawdlerContext()
				.getAttribute(ServiceBase.SERVICES_MANAGER);
		RemoteService remoteService = type.getAnnotation(RemoteService.class);
		if (remoteService != null) {
			String serviceName = remoteService.serviceName();
			if (serviceName.trim().equals("")) {
				servicesManager.registerService(type, target, remoteService.single());
			} else {
				servicesManager.register(serviceName, target, remoteService.single());
			}
		} else {
			Class<?>[] interfaceList = type.getInterfaces();
			for (Class<?> clazz : interfaceList) {
				remoteService = clazz.getAnnotation(RemoteService.class);
				if (remoteService == null) {
					continue;
				}
				String serviceName = remoteService.serviceName();
				if (serviceName.trim().equals("")) {
					servicesManager.registerService(clazz, target, remoteService.single());
				} else {
					servicesManager.register(serviceName, target, remoteService.single());
				}
			}
		}

	}

	@Override
	public Class<?>[] getMatchTypes() {
		return null;
	}

	@Override
	public Set<? extends Class<? extends Annotation>> getMatchAnnotations() {
		Set<Class<RemoteService>> annotationSet = new HashSet<>();
		annotationSet.add(RemoteService.class);
		return annotationSet;
	}

	@Override
	public String[] scanLocations() {
		return new String[] { "com.anywide.dawdler.serverplug.service.impl" };
	}

}
