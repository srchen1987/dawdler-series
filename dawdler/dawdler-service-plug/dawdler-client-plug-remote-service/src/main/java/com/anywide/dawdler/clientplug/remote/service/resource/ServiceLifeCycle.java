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
package com.anywide.dawdler.clientplug.remote.service.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.ConnectionPool;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceLifeCycle.java
 * @Description service销毁后关闭ConnectionPool(老版本在com.anywide.dawdler.clientplug.web.listener.WebListener中关闭)
 * @date 2024年2月9日
 * @email suxuan696@gmail.com
 */
@Order(Integer.MAX_VALUE)
public class ServiceLifeCycle implements ComponentLifeCycle {
	private static final Logger logger = LoggerFactory.getLogger(ServiceLifeCycle.class);

	@Override
	public void prepareDestroy() throws Throwable {
		try {
			ConnectionPool.shutdown();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
