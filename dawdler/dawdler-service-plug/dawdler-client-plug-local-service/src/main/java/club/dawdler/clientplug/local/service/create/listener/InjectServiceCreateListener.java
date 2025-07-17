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
package club.dawdler.clientplug.local.service.create.listener;

import java.util.List;

import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFireHolder;
import club.dawdler.core.order.OrderData;
import club.dawdler.core.service.listener.DawdlerServiceCreateListener;

/**
 * @author jackson.song
 * @version V1.0
 * 实现Service的组件注入
 */
public class InjectServiceCreateListener implements DawdlerServiceCreateListener {
	private final List<OrderData<RemoteClassLoaderFire>> fireList = RemoteClassLoaderFireHolder.getInstance()
			.getRemoteClassLoaderFire();

	@Override
	public void create(Object service, boolean single) throws Throwable {
		for (OrderData<RemoteClassLoaderFire> rf : fireList) {
			rf.getData().onLoadFire(service.getClass(), service);
		}
	}

}
