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
package club.dawdler.serverplug.conf.consul.resource;

import java.util.List;

import club.dawdler.conf.Refresher;
import club.dawdler.conf.init.ConfigInit;
import club.dawdler.core.annotation.Order;
import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.order.OrderData;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.server.deploys.ServiceBase;
import club.dawdler.server.filter.DawdlerFilter;
import club.dawdler.server.filter.FilterProvider;
import club.dawdler.server.listener.DawdlerListenerProvider;
import club.dawdler.server.listener.DawdlerServiceListener;

/**
 * @author jackson.song
 * @version V1.0
 * 服务器端初始化与销毁
 */
@Order(1)
public class ConfigLifeCycle implements ComponentLifeCycle {

	@Override
	public void prepareInit() throws Throwable {
		ConfigInit.getInstance().init();
	}

	public void init(List<OrderData<DawdlerServiceListener>> dawdlerServiceListeners,
			List<OrderData<DawdlerFilter>> dawdlerFilters) throws Exception {
		for (OrderData<DawdlerServiceListener> orderData : dawdlerServiceListeners) {
			Refresher.refreshAllConfig(orderData.getData());
		}

		for (OrderData<DawdlerFilter> orderData : dawdlerFilters) {
			Refresher.refreshAllConfig(orderData.getData());
		}
	}

	public void destroy() {
		ConfigInit.getInstance().destroy();
	}

	@Override
	public void init() throws Exception {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		FilterProvider filterProvider = (FilterProvider) dawdlerContext.getAttribute(ServiceBase.FILTER_PROVIDER);
		DawdlerListenerProvider dawdlerListenerProvider = (DawdlerListenerProvider) dawdlerContext
				.getAttribute(ServiceBase.DAWDLER_LISTENER_PROVIDER);
		init(dawdlerListenerProvider.getListeners(), filterProvider.getFilters());
	}

}
