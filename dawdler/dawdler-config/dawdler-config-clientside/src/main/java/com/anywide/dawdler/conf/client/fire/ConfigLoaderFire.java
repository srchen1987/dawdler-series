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
package com.anywide.dawdler.conf.client.fire;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoderFire;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.handler.AnnotationUrlHandler;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.interceptor.InterceptorProvider;
import com.anywide.dawdler.conf.Refresher;
import com.anywide.dawdler.conf.cache.PathMappingTargetCache;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConfigLoaderFire.java
 * @Description 获取远程类加载时触发通知，实现动态注入config
 * @date 2021年05月30日
 * @email suxuan696@gmail.com
 */
@Order(1)
public class ConfigLoaderFire implements RemoteClassLoderFire {
	private static Logger logger = LoggerFactory.getLogger(ConfigLoaderFire.class);

	@Override
	public void onLoadFire(Class<?> clazz, byte[] classCodes) {
		try {
			refreshMappingConfig(clazz);
		} catch (Exception e) {
			logger.error("", e);
		}
		try {
			refreshInterceptorConfig(clazz);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public void onRemoveFire(Class<?> clazz) {
		removeMappingConfig(clazz);
		removeInterceptorConfig(clazz);
	}

	private void refreshMappingConfig(Class<?> clazz) throws Exception {
		if (TransactionController.class.isAssignableFrom(clazz)) {
			for (TransactionController transactionController : AnnotationUrlHandler.getTransactionControllers()) {
				if (transactionController.getClass() == clazz) {
					Refresher.refreshAllConfig(transactionController);
					return;
				}
			}
		}
	}

	public void removeMappingConfig(Class<?> clazz) {
		if (TransactionController.class.isAssignableFrom(clazz)) {
			PathMappingTargetCache.removeMappingByTargetClass(clazz);
		}
	}

	private void refreshInterceptorConfig(Class<?> clazz) throws Exception {
		if (HandlerInterceptor.class.isAssignableFrom(clazz)) {
			List<OrderData<HandlerInterceptor>> interceptors = InterceptorProvider.getHandlerInterceptors();
			for (OrderData<HandlerInterceptor> interceptor : interceptors) {
				if (interceptor.getData().getClass() == clazz) {
					Refresher.refreshAllConfig(interceptor.getData());
				}
			}
		}
	}

	public void removeInterceptorConfig(Class<?> clazz) {
		if (HandlerInterceptor.class.isAssignableFrom(clazz)) {
			PathMappingTargetCache.removeMappingByTargetClass(clazz);
		}
	}

}
