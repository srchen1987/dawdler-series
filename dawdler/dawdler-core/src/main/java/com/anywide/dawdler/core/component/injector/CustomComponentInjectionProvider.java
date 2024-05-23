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
package com.anywide.dawdler.core.component.injector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * 自定义组件注入接口提供者
 */
public class CustomComponentInjectionProvider {
	private static Map<String, CustomComponentInjectionProvider> instances = new ConcurrentHashMap<>();
	private final List<OrderData<CustomComponentInjector>> customComponentInjectors = new ArrayList<>();

	public static CustomComponentInjectionProvider getDefaultInstance() {
		return getInstance(CustomComponentInjectionProvider.class.getName());
	}

	public static CustomComponentInjectionProvider getInstance(String serviceName) {
		CustomComponentInjectionProvider componentLifeCycleProvider = instances.get(serviceName);
		if (componentLifeCycleProvider != null) {
			return componentLifeCycleProvider;
		}
		synchronized (instances) {
			componentLifeCycleProvider = instances.get(serviceName);
			if (componentLifeCycleProvider == null) {
				instances.put(serviceName, new CustomComponentInjectionProvider());
			}
		}
		return instances.get(serviceName);
	}

	private CustomComponentInjectionProvider() {
		ServiceLoader.load(CustomComponentInjector.class).forEach(customComponentInjector -> {
			OrderData<CustomComponentInjector> orderData = new OrderData<>();
			Order order = customComponentInjector.getClass().getAnnotation(Order.class);
			if (order != null) {
				orderData.setOrder(order.value());
			}
			orderData.setData(customComponentInjector);
			customComponentInjectors.add(orderData);
		});
		OrderComparator.sort(customComponentInjectors);
	}

	public List<OrderData<CustomComponentInjector>> getCustomComponentInjectors() {
		return customComponentInjectors;
	}

}
