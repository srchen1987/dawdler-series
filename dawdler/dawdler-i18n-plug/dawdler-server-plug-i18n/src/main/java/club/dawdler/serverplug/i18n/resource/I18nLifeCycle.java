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
package club.dawdler.serverplug.i18n.resource;

import java.util.List;
import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.order.OrderData;
import club.dawdler.i18n.I18nOperatorFactory;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.server.deploys.ServiceBase;
import club.dawdler.server.filter.DawdlerFilter;
import club.dawdler.server.filter.FilterProvider;
import club.dawdler.server.listener.DawdlerListenerProvider;
import club.dawdler.server.listener.DawdlerServiceListener;

/**
 * @author jackson.song
 * @version V1.0
 * 国际化生命周期组件 实现向监听器、过滤器和服务注入I18nOperator，确保这些组件能够使用国际化功能。
 */
public class I18nLifeCycle implements ComponentLifeCycle {

	/**
	 * 初始化方法，在dawdler容器启动时自动调用
	 * 
	 * 获取容器中的监听器和过滤器列表，并为它们注入I18nOperator实例。
	 * 
	 * @throws Throwable 初始化过程中可能抛出的异常
	 */
	@Override
	public void init() throws Throwable {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		FilterProvider filterProvider = (FilterProvider) dawdlerContext.getAttribute(ServiceBase.FILTER_PROVIDER);
		DawdlerListenerProvider dawdlerListenerProvider = (DawdlerListenerProvider) dawdlerContext
				.getAttribute(ServiceBase.DAWDLER_LISTENER_PROVIDER);
		init(dawdlerListenerProvider.getListeners(), filterProvider.getFilters());
	}

	/**
	 * 为监听器和过滤器注入I18nOperator实例
	 * 
	 * @param dawdlerServiceListeners 监听器列表
	 * @param dawdlerFilters          过滤器列表
	 * @throws Throwable 注入过程中可能抛出的异常
	 */
	public void init(List<OrderData<DawdlerServiceListener>> dawdlerServiceListeners,
			List<OrderData<DawdlerFilter>> dawdlerFilters) throws Throwable {
		for (OrderData<DawdlerServiceListener> orderData : dawdlerServiceListeners) {
			DawdlerServiceListener dawdlerServiceListener = orderData.getData();
			I18nOperatorFactory.initField(dawdlerServiceListener, dawdlerServiceListener.getClass());
		}
		for (OrderData<DawdlerFilter> orderData : dawdlerFilters) {
			DawdlerFilter dawdlerFilter = orderData.getData();
			I18nOperatorFactory.initField(dawdlerFilter, dawdlerFilter.getClass());
		}
	}

}