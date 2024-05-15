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
package com.anywide.dawdler.serverplug.discovery.consul.resource;

import java.util.HashMap;
import java.util.Map;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.discovery.consul.ConsulDiscoveryCenter;
import com.anywide.dawdler.core.discovery.consul.ConsulDiscoveryCenter.HealthCheckTypes;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.server.conf.ServerConfig.HealthCheck;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.plug.discoverycenter.AbstractServerDiscoveryCenterLifeCycle;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConsulLifeCycle.java
 * @Description consul注册中心初始化与销毁
 * @date 2023年3月5日
 * @email suxuan696@gmail.com
 */
@Order(com.anywide.dawdler.core.order.Order.LOWEST_PRECEDENCE)
public class ConsulLifeCycle extends AbstractServerDiscoveryCenterLifeCycle {

	@Override
	public void afterInit() throws Throwable {
		ConsulDiscoveryCenter discoveryCenter = ConsulDiscoveryCenter.getInstance();
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		Map<String, Object> attributes = new HashMap<>();
		if (discoveryCenter.getHealthCheckType().equals(HealthCheckTypes.HTTP.name())) {
			HealthCheck healthCheck = dawdlerContext.getHealthCheck();
			if (!healthCheck.isCheck()) {
				throw new java.lang.IllegalArgumentException(
						"use consul to discovery-center must open health-check in server-conf.xml!");
			}
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_PORT, healthCheck.getPort());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_SCHEME, healthCheck.getScheme());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_USERNAME, healthCheck.getUsername());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_PASSWORD, healthCheck.getPassword());
		}
		addProvider(attributes);
	}

	@Override
	public DiscoveryCenter getDiscoveryCenter() throws Exception {
		return ConsulDiscoveryCenter.getInstance();
	}
}
