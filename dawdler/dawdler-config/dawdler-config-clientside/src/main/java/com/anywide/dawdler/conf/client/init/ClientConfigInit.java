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
package com.anywide.dawdler.conf.client.init;

import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.handler.AnnotationUrlHandler;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.interceptor.InterceptorProvider;
import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.clientplug.web.listener.WebContextListenerProvider;
import com.anywide.dawdler.conf.Refresher;
import com.anywide.dawdler.conf.init.ConfigInit;
import com.anywide.dawdler.core.order.OrderData;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientConfigInit.java
 * @Description client端配置中心初始化
 * @date 2021年5月30日
 * @email suxuan696@gmail.com
 */
public class ClientConfigInit {

	private static ConfigInit configInit = new ConfigInit();

	public static void init() throws Exception {
		configInit.init();
		for (OrderData<WebContextListener> orderData : WebContextListenerProvider.getWebContextListeners()) {
			Refresher.refreshAllConfig(orderData.getData());
		}

		for (OrderData<HandlerInterceptor> orderData : InterceptorProvider.getHandlerInterceptors()) {
			Refresher.refreshAllConfig(orderData.getData());
		}

		for (TransactionController transactionController : AnnotationUrlHandler.getTransactionControllers()) {
			Refresher.refreshAllConfig(transactionController);
		}
	}

	public static void destroy() {
		configInit.destroy();
	}

}
