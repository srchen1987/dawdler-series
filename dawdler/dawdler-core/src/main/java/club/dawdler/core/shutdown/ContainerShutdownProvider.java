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
package club.dawdler.core.shutdown;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import club.dawdler.core.annotation.Order;
import club.dawdler.core.order.OrderComparator;
import club.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * ContainerShutdown提供者 SPI方式接入
 */
public class ContainerShutdownProvider {

	private static final ContainerShutdownProvider INSTANCE = new ContainerShutdownProvider();
	private final List<OrderData<ContainerGracefulShutdown>> containerShutdownList = new ArrayList<>();

	private ContainerShutdownProvider() {
		ServiceLoader.load(ContainerGracefulShutdown.class).forEach(containerShutdown -> {
			Order order = containerShutdown.getClass().getAnnotation(Order.class);
			OrderData<ContainerGracefulShutdown> orderData = new OrderData<>();
			orderData.setData(containerShutdown);
			if (order != null) {
				orderData.setOrder(order.value());
			}
			containerShutdownList.add(orderData);
		});
		OrderComparator.sort(containerShutdownList);
	}

	public static ContainerShutdownProvider getInstance() {
		return INSTANCE;
	}

	public List<OrderData<ContainerGracefulShutdown>> getContainerShutdownList() {
		return containerShutdownList;
	}

}
