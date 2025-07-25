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
package club.dawdler.server.deploys;

import club.dawdler.core.health.ServiceHealth;
import club.dawdler.core.service.bean.ServicesBean;
import club.dawdler.core.service.processor.ServiceExecutor;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.server.filter.FilterProvider;

/**
 * @author jackson.song
 * @version V1.0
 * deploy下服务模块定义的接口
 */
public interface Service {
	public static final String FILTER_PROVIDER = "filterProvider";
	public static final String DAWDLER_LISTENER_PROVIDER = "dawdlerListenerProvider";

	void start() throws Throwable;

	void stop();

	void prepareStop();

	ServicesBean getServicesBean(String name);

	DawdlerContext getDawdlerContext();

	ServiceExecutor getServiceExecutor();

	FilterProvider getFilterProvider();

	public ServiceHealth getServiceHealth();

	public void status(String status);

	public void cause(Throwable throwable);

	public String getStatus();

	public Throwable getCause();

	public ClassLoader getClassLoader();
}
