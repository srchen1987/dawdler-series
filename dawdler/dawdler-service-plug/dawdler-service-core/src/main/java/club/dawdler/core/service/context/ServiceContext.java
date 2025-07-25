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
package club.dawdler.core.service.context;

import club.dawdler.core.context.DawdlerRuntimeContext;
import club.dawdler.core.service.ServicesManager;
import club.dawdler.core.service.bean.ServicesBean;
import club.dawdler.core.service.listener.DawdlerServiceCreateProvider;

/**
 * @author jackson.song
 * @version V1.0
 * 服务层上下文的抽象类
 */
public abstract class ServiceContext extends DawdlerRuntimeContext {
	protected final ServicesManager servicesManager;
	

	protected ServiceContext(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	public DawdlerServiceCreateProvider getDawdlerServiceCreateProvider() {
		return servicesManager.getDawdlerServiceCreateProvider();
	}

	public ServicesBean getServicesBean(String serviceName) {
		return servicesManager.getService(serviceName);
	}


	public ServicesManager getServicesManager() {
		return servicesManager;
	}

}
