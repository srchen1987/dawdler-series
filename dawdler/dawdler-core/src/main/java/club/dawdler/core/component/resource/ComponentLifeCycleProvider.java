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
package club.dawdler.core.component.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import club.dawdler.core.annotation.Order;
import club.dawdler.core.order.OrderComparator;
import club.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * 组件生命周期接口提供者
 */
public class ComponentLifeCycleProvider {
	private static Map<String, ComponentLifeCycleProvider> instances = new ConcurrentHashMap<>();
	private final List<OrderData<ComponentLifeCycle>> componentLifeCycles = new ArrayList<>();

	public static ComponentLifeCycleProvider getInstance(String serviceName) {
		ComponentLifeCycleProvider componentLifeCycleProvider = instances.get(serviceName);
		if (componentLifeCycleProvider != null) {
			return componentLifeCycleProvider;
		}
		synchronized (instances) {
			componentLifeCycleProvider = instances.get(serviceName);
			if (componentLifeCycleProvider == null) {
				instances.put(serviceName, new ComponentLifeCycleProvider());
			}
		}

		return instances.get(serviceName);
	}

	private ComponentLifeCycleProvider() {
		ServiceLoader.load(ComponentLifeCycle.class).forEach(componentLifeCycle -> {
			OrderData<ComponentLifeCycle> orderData = new OrderData<>();
			Order order = componentLifeCycle.getClass().getAnnotation(Order.class);
			if (order != null) {
				orderData.setOrder(order.value());
			}
			orderData.setData(componentLifeCycle);
			componentLifeCycles.add(orderData);
		});
		OrderComparator.sort(componentLifeCycles);
	}

	public List<OrderData<ComponentLifeCycle>> getComponentLifeCycles() {
		return componentLifeCycles;
	}
}
