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
package com.anywide.dawdler.serverplug.local.service.resource;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.local.service.injector.LocalServiceInjector;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.ServiceBase;
import com.anywide.dawdler.server.filter.DawdlerFilter;
import com.anywide.dawdler.server.filter.FilterProvider;
import com.anywide.dawdler.server.listener.DawdlerListenerProvider;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;

/**
 * @author jackson.song
 * @version V1.0
 * @Title LocalServiceLifeCycle.java
 * @Description 服务器端初始化与销毁
 * @date 2021年5月30日
 * @email suxuan696@gmail.com
 */
@Order(1)
public class LocalServiceLifeCycle implements ComponentLifeCycle {

	@Override
	public void init() throws Throwable {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		FilterProvider filterProvider = (FilterProvider) dawdlerContext.getAttribute(ServiceBase.FILTER_PROVIDER);
		DawdlerListenerProvider dawdlerListenerProvider = (DawdlerListenerProvider) dawdlerContext
				.getAttribute(ServiceBase.DAWDLER_LISTENER_PROVIDER);
		
		for (OrderData<DawdlerServiceListener> orderData : dawdlerListenerProvider.getListeners()) {
			LocalServiceInjector.injectLocalService(orderData.getData(), dawdlerContext);
		}

		for (OrderData<DawdlerFilter> orderData : filterProvider.getFilters()) {
			LocalServiceInjector.injectLocalService(orderData.getData(), dawdlerContext);
		}
		
	}

}
