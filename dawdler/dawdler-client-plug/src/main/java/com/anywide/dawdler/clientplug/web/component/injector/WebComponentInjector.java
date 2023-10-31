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
package com.anywide.dawdler.clientplug.web.component.injector;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.anywide.dawdler.clientplug.annotation.Controller;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFireHolder;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebComponentInjector.java
 * @Description 注入web组件 Controller WebContextListener HandlerInterceptor
 * @date 2023年7月20日
 * @email suxuan696@gmail.com
 */
@Order(1)
public class WebComponentInjector implements CustomComponentInjector{
	private final List<OrderData<RemoteClassLoaderFire>> fireList = RemoteClassLoaderFireHolder.getInstance()
			.getRemoteClassLoaderFire();
	@Override
	public void inject(Class<?> type, Object target) throws Throwable {
		for (OrderData<RemoteClassLoaderFire> rf : fireList) {
			rf.getData().onLoadFire(type, target);
		}
	}

	@Override
	public Class<?>[] getMatchTypes() {
		return new Class<?>[] { WebContextListener.class , HandlerInterceptor.class};
	}

	@Override
	public Set<? extends Class<? extends Annotation>> getMatchAnnotations() {
		Set<Class<? extends Annotation>> annotationSet = new HashSet<>();
		annotationSet.add(Controller.class);
		return annotationSet;
	}
	
}
