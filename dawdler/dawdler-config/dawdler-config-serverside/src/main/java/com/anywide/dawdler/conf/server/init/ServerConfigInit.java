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
package com.anywide.dawdler.conf.server.init;

import java.util.List;

import com.anywide.dawdler.conf.Refresher;
import com.anywide.dawdler.conf.init.ConfigInit;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.server.filter.DawdlerFilter;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServerConfigInit.java
 * @Description 服务器端初始化类
 * @date 2021年05月30日
 * @email suxuan696@gmail.com
 */
public class ServerConfigInit {

	private static ConfigInit configInit = new ConfigInit();

	public static void init(List<OrderData<DawdlerServiceListener>> dawdlerServiceListeners,
			List<OrderData<DawdlerFilter>> dawdlerFilters) throws Exception {
		configInit.init();
		for (OrderData<DawdlerServiceListener> orderData : dawdlerServiceListeners) {
			Refresher.refreshAllConfig(orderData.getData());
		}

		for (OrderData<DawdlerFilter> orderData : dawdlerFilters) {
			Refresher.refreshAllConfig(orderData.getData());
		}
	}

	public static void destroy() {
		configInit.destroy();
	}

}
