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
package club.dawdler.serverplug.jedis.resource;

import java.util.List;

import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.order.OrderData;
import club.dawdler.jedis.JedisOperatorFactory;
import club.dawdler.jedis.lock.JedisDistributedLockHolderFactory;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.server.deploys.ServiceBase;
import club.dawdler.server.filter.DawdlerFilter;
import club.dawdler.server.filter.FilterProvider;
import club.dawdler.server.listener.DawdlerListenerProvider;
import club.dawdler.server.listener.DawdlerServiceListener;

/**
 * @author jackson.song
 * @version V1.0
 * 实现向监听器,过滤器注入JedisOperator,JedisDistributedLock
 */
public class JedisLifeCycle implements ComponentLifeCycle {

	@Override
	public void init() throws Throwable {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		FilterProvider filterProvider = (FilterProvider) dawdlerContext.getAttribute(ServiceBase.FILTER_PROVIDER);
		DawdlerListenerProvider dawdlerListenerProvider = (DawdlerListenerProvider) dawdlerContext
				.getAttribute(ServiceBase.DAWDLER_LISTENER_PROVIDER);
		init(dawdlerListenerProvider.getListeners(), filterProvider.getFilters());
	}

	public void init(List<OrderData<DawdlerServiceListener>> dawdlerServiceListeners,
			List<OrderData<DawdlerFilter>> dawdlerFilters) throws Throwable {
		for (OrderData<DawdlerServiceListener> orderData : dawdlerServiceListeners) {
			DawdlerServiceListener dawdlerServiceListener = orderData.getData();
			JedisOperatorFactory.initField(dawdlerServiceListener, dawdlerServiceListener.getClass());
			JedisDistributedLockHolderFactory.initField(dawdlerServiceListener, dawdlerServiceListener.getClass());
		}
		for (OrderData<DawdlerFilter> orderData : dawdlerFilters) {
			DawdlerFilter dawdlerFilter = orderData.getData();
			JedisOperatorFactory.initField(dawdlerFilter, dawdlerFilter.getClass());
			JedisDistributedLockHolderFactory.initField(dawdlerFilter, dawdlerFilter.getClass());
		}
	}

}
