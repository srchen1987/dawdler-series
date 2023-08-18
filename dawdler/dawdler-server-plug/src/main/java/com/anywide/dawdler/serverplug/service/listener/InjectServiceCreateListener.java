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
package com.anywide.dawdler.serverplug.service.listener;

import java.lang.reflect.Field;

import com.anywide.dawdler.client.ServiceFactory;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.annotation.Service;
import com.anywide.dawdler.core.exception.NotSetRemoteServiceException;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateListener;

/**
 * @author jackson.song
 * @version V1.0
 * @Title InjectServiceCreateListener.java
 * @Description 监听器实现service和service的注入
 * @date 2015年7月8日
 * @email suxuan696@gmail.com
 */
public class InjectServiceCreateListener implements DawdlerServiceCreateListener {

	@Override
	public void create(Object service, boolean single, DawdlerContext dawdlerContext) throws Throwable {
		inject(service, dawdlerContext);
	}

	private void inject(Object service, DawdlerContext dawdlerContext) throws Throwable {
		Field[] fields = service.getClass().getDeclaredFields();
		for (Field field : fields) {
			Service serviceAnnotation = field.getAnnotation(Service.class);
			Class<?> serviceClass = field.getType();
			field.setAccessible(true);
			if (serviceAnnotation != null) {
				if (serviceAnnotation.remote()) {
					RemoteService remoteService = serviceClass.getAnnotation(RemoteService.class);
					if (remoteService == null) {
						throw new NotSetRemoteServiceException("not found @RemoteService on " + serviceClass.getName());
					}
					String groupName = remoteService.value();
					field.set(service, ServiceFactory.getService(serviceClass, groupName, remoteService.loadBalance(),
							dawdlerContext.getClassLoader()));
				} else {
					field.set(service, dawdlerContext.getServiceProxy(serviceClass));
				}
			}
		}
	}
}
