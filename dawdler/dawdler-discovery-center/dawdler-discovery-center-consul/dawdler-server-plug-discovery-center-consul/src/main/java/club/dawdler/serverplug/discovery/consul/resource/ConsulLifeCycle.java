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
package club.dawdler.serverplug.discovery.consul.resource;

import java.util.HashMap;
import java.util.Map;

import club.dawdler.core.annotation.Order;
import club.dawdler.core.discovery.consul.ConsulDiscoveryCenter;
import club.dawdler.core.discovery.consul.ConsulDiscoveryCenter.HealthCheckTypes;
import club.dawdler.core.discoverycenter.DiscoveryCenter;
import club.dawdler.server.conf.ServerConfig.HealthCheck;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.server.plug.discoverycenter.AbstractServerDiscoveryCenterLifeCycle;

/**
 * @author jackson.song
 * @version V1.0
 * consul注册中心初始化与销毁
 */
@Order(club.dawdler.core.order.Order.LOWEST_PRECEDENCE)
public class ConsulLifeCycle extends AbstractServerDiscoveryCenterLifeCycle {

	@Override
	public void afterInit() throws Throwable {
		ConsulDiscoveryCenter discoveryCenter = ConsulDiscoveryCenter.getInstance();
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		Map<String, Object> attributes = new HashMap<>();
		if (discoveryCenter.getHealthCheckType().equals(HealthCheckTypes.HTTP.getName())) {
			HealthCheck healthCheck = dawdlerContext.getHealthCheck();
			if (!healthCheck.isCheck()) {
				throw new java.lang.IllegalArgumentException(
						"use consul to discovery-center must open health-check in server-conf.xml!");
			}
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_PORT, healthCheck.getPort());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_SCHEME, healthCheck.getScheme());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_USERNAME, healthCheck.getUsername());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_PASSWORD, healthCheck.getPassword());
			attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_URI, healthCheck.getUri());
		}
		addProvider(attributes);
	}

	@Override
	public DiscoveryCenter getDiscoveryCenter() throws Exception {
		return ConsulDiscoveryCenter.getInstance();
	}
}
