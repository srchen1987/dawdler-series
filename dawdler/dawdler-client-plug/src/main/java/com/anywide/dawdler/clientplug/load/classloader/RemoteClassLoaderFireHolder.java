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
package com.anywide.dawdler.clientplug.load.classloader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RemoteClassLoaderFireHolder.java
 * @Description 远程类加载时触发持有者
 * @date 2007年9月13日
 * @email suxuan696@gmail.com
 */
public class RemoteClassLoaderFireHolder {
	private static final RemoteClassLoaderFireHolder remoteClassLoaderFireHolder = new RemoteClassLoaderFireHolder();
	private final List<OrderData<RemoteClassLoaderFire>> fires = new ArrayList<>();

	public static RemoteClassLoaderFireHolder getInstance() {
		return remoteClassLoaderFireHolder;
	}

	private RemoteClassLoaderFireHolder() {
		ServiceLoader.load(RemoteClassLoaderFire.class).forEach(service -> {
			OrderData<RemoteClassLoaderFire> orderData = new OrderData<>();
			Order order = service.getClass().getAnnotation(Order.class);
			if (order != null) {
				orderData.setOrder(order.value());
			}
			orderData.setData(service);
			fires.add(orderData);
		});
		OrderComparator.sort(fires);
	}

	List<OrderData<RemoteClassLoaderFire>> getRemoteClassLoaderFire() {
		return fires;
	}
}
