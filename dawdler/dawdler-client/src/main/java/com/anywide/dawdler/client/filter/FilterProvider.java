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
package com.anywide.dawdler.client.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.order.OrderComparator;
import com.anywide.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V2.0
 * @Title FilterProvider.java
 * @Description 过滤器提供者
 * @date 2015年4月6日
 * @email suxuan696@gmail.com modify 2016年5月22日
 */
public class FilterProvider {
	private static final List<OrderData<DawdlerClientFilter>> filters = new ArrayList<>();
	public static FilterChain lastChain;

	static {
		ServiceLoader<DawdlerClientFilter> loader = ServiceLoader.load(DawdlerClientFilter.class);
		loader.forEach(FilterProvider::addFilters);
		order();
		FilterChain chain = new DefaultFilterChain();
		lastChain = buildChain(chain);
	}

	static void addFilters(DawdlerClientFilter filter) {
		Order co = filter.getClass().getAnnotation(Order.class);
		OrderData<DawdlerClientFilter> od = new OrderData<>();
		od.setData(filter);
		if (co != null) {
			od.setOrder(co.value());
		}
		filters.add(od);
	}

	static void order() {
		OrderComparator.sort(filters);
	}

	static FilterChain buildChain(final FilterChain chain) {
		FilterChain last = chain;
		if (filters.size() > 0) {
			for (int i = filters.size() - 1; i >= 0; i--) {
				final DawdlerClientFilter filter = filters.get(i).getData();
				final FilterChain next = last;
				last = request -> filter.doFilter(request, next);
			}
		}
		return last;
	}

	public static Object doFilter(RequestBean request) throws Exception {
		return lastChain.doFilter(request);
	}

}
