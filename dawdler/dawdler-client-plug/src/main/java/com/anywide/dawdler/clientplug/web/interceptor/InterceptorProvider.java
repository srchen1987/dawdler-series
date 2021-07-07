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
package com.anywide.dawdler.clientplug.web.interceptor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * @Title InterceptorProvider.java
 * @Description 拦截器提供者
 * @date 2007年04月19日
 * @email suxuan696@gmail.com
 */
public class InterceptorProvider {
	private static final List<OrderData<HandlerInterceptor>> handlerInterceptors = new CopyOnWriteArrayList<>();

	public static List<OrderData<HandlerInterceptor>> getHandlerInterceptors() {
		return handlerInterceptors;
	}

	public static void addHandlerInterceptor(HandlerInterceptor handlerInterceptor) {
		Order order = handlerInterceptor.getClass().getAnnotation(Order.class);
		OrderData<HandlerInterceptor> orderData = new OrderData<>();
		orderData.setData(handlerInterceptor);
		if (order != null) {
			orderData.setOrder(order.value());
		}
		handlerInterceptors.add(orderData);
	}

	public static void removeHandlerInterceptor(Class<?> handlerInterceptorClass) {
		if (!HandlerInterceptor.class.isAssignableFrom(handlerInterceptorClass))
			return;
		for (OrderData<HandlerInterceptor> orderData : handlerInterceptors) {
			if (orderData.getData().getClass() == handlerInterceptorClass) {
				handlerInterceptors.remove(orderData);
				return;
			}
		}
	}

	public static void order() {
		OrderComparator.sort(handlerInterceptors);
	}

}
