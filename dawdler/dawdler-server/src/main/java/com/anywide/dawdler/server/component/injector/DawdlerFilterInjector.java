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
package com.anywide.dawdler.server.component.injector;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.ServiceBase;
import com.anywide.dawdler.server.filter.DawdlerFilter;
import com.anywide.dawdler.server.filter.FilterProvider;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerFilterInjector.java
 * @Description 注入DawdlerFilter
 * @date 2023年7月20日
 * @email suxuan696@gmail.com
 */
@Order(1)
public class DawdlerFilterInjector implements CustomComponentInjector {

	@Override
	public void inject(Class<?> type, Object target) throws Throwable {
		Order order = type.getAnnotation(Order.class);
		DawdlerFilter filter = (DawdlerFilter) target;
		OrderData<DawdlerFilter> orderData = new OrderData<>();
		orderData.setData(filter);
		if (order != null) {
			orderData.setOrder(order.value());
		}
		((FilterProvider) DawdlerContext.getDawdlerContext().getAttribute(ServiceBase.FILTER_PROVIDER))
				.addFilter(filter);
	}

	@Override
	public Class<?>[] getMatchTypes() {
		return new Class<?>[] { DawdlerFilter.class };
	}

}
