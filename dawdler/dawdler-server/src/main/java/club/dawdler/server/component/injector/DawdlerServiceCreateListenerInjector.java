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
package club.dawdler.server.component.injector;

import club.dawdler.core.annotation.Order;
import club.dawdler.core.component.injector.CustomComponentInjector;
import club.dawdler.core.service.ServicesManager;
import club.dawdler.core.service.listener.DawdlerServiceCreateListener;
import club.dawdler.server.context.DawdlerContext;

/**
 * @author jackson.song
 * @version V1.0
 * 注入服务创建监听器
 */
@Order(1)
public class DawdlerServiceCreateListenerInjector implements CustomComponentInjector {

	@Override
	public void inject(Class<?> type, Object target) throws Throwable {
		ServicesManager servicesManager = DawdlerContext.getDawdlerContext().getServicesManager();
		servicesManager.getDawdlerServiceCreateProvider().addServiceCreate((DawdlerServiceCreateListener) target);
	}

	@Override
	public Class<?>[] getMatchTypes() {
		return new Class<?>[] { DawdlerServiceCreateListener.class };
	}

	@Override
	public String[] scanLocations() {
		return new String[] { "club.dawdler.serverplug.**.listener" };
	}

}
