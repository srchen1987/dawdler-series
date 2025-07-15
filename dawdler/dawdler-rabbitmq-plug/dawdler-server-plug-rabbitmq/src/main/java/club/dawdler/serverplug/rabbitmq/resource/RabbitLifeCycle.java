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
package club.dawdler.serverplug.rabbitmq.resource;

import java.util.List;

import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.order.OrderData;
import club.dawdler.rabbitmq.consumer.RabbitListenerInit;
import club.dawdler.rabbitmq.provider.RabbitProviderFactory;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.server.deploys.ServiceBase;
import club.dawdler.server.filter.DawdlerFilter;
import club.dawdler.server.filter.FilterProvider;
import club.dawdler.server.listener.DawdlerListenerProvider;
import club.dawdler.server.listener.DawdlerServiceListener;

/**
 * @author jackson.song
 * @version V1.0
 * 实现向监听器,过滤器注入RabbitProvider与RabbitListener注解方法
 */
public class RabbitLifeCycle implements ComponentLifeCycle {
	@Override
	public void init() throws Exception {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		FilterProvider filterProvider = (FilterProvider) dawdlerContext.getAttribute(ServiceBase.FILTER_PROVIDER);
		DawdlerListenerProvider dawdlerListenerProvider = (DawdlerListenerProvider) dawdlerContext
				.getAttribute(ServiceBase.DAWDLER_LISTENER_PROVIDER);
		init(dawdlerListenerProvider.getListeners(), filterProvider.getFilters());
	}

	public void init(List<OrderData<DawdlerServiceListener>> dawdlerServiceListeners,
			List<OrderData<DawdlerFilter>> dawdlerFilters) throws Exception {
		for (OrderData<DawdlerServiceListener> orderData : dawdlerServiceListeners) {
			DawdlerServiceListener dawdlerServiceListener = orderData.getData();
			RabbitProviderFactory.initField(dawdlerServiceListener, dawdlerServiceListener.getClass());
			RabbitListenerInit.initRabbitListener(dawdlerServiceListener, dawdlerServiceListener.getClass());
		}
		for (OrderData<DawdlerFilter> orderData : dawdlerFilters) {
			DawdlerFilter dawdlerFilter = orderData.getData();
			RabbitProviderFactory.initField(dawdlerFilter, dawdlerFilter.getClass());
			RabbitListenerInit.initRabbitListener(dawdlerFilter, dawdlerFilter.getClass());
		}
	}

}
