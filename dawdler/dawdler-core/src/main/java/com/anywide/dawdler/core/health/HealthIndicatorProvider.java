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
package com.anywide.dawdler.core.health;

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
 * HealthIndicatorProvider 检测提供者 通过SPI接入
 */
public class HealthIndicatorProvider {
	private final List<OrderData<HealthIndicator>> indicators = new ArrayList<>();
	private static Map<String, HealthIndicatorProvider> instances = new ConcurrentHashMap<>();

	public static HealthIndicatorProvider getInstance(String serviceName) {
		HealthIndicatorProvider healthChecker = instances.get(serviceName);
		if (healthChecker != null) {
			return healthChecker;
		}
		synchronized (instances) {
			healthChecker = instances.get(serviceName);
			if (healthChecker == null) {
				instances.put(serviceName, new HealthIndicatorProvider());
			}
		}
		return instances.get(serviceName);
	}

	private HealthIndicatorProvider() {
		ServiceLoader.load(HealthIndicator.class).forEach(indicator -> {
			OrderData<HealthIndicator> orderData = new OrderData<>();
			Order order = indicator.getClass().getAnnotation(Order.class);
			if (order != null) {
				orderData.setOrder(order.value());
			}
			orderData.setData(indicator);
			indicators.add(orderData);
		});
		OrderComparator.sort(indicators);
	}

	public List<OrderData<HealthIndicator>> getHealthIndicators() {
		return indicators;
	}

}
