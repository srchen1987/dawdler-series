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
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.discovery.consul.ConsulDiscoveryCenter;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.server.conf.ServerConfig.HealthCheck;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.util.HashedWheelTimer;
import com.anywide.dawdler.util.Timeout;
import com.anywide.dawdler.util.TimerTask;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConsulLifeCycle
 * @Description consul注册中心初始化与销毁
 * @date 2023年3月5日
 * @email suxuan696@gmail.com
 */
@Order(Integer.MAX_VALUE)
public class ConsulLifeCycle implements ComponentLifeCycle {
	private static final Logger logger = LoggerFactory.getLogger(ConsulLifeCycle.class);
	private DiscoveryCenter discoveryCenter;
	private Timeout timeout;
	private long checkTime = 5000;
	private HashedWheelTimer hashedWheelTimer;

	@Override
	public void afterInit() throws Throwable {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		HealthCheck healthCheck = dawdlerContext.getHealthCheck();
		if (!healthCheck.isCheck()) {
			throw new java.lang.IllegalArgumentException(
					"use consul to discovery-center must open health-check in conf/server-conf.xml!");
		}
		String channelGroup = dawdlerContext.getDeployName();
		discoveryCenter = ConsulDiscoveryCenter.getInstance();
		String path = channelGroup;
		String value = dawdlerContext.getHost() + ":" + dawdlerContext.getPort();
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_PORT, healthCheck.getPort());
		attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_SCHEME, healthCheck.getScheme());
		attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_USERNAME, healthCheck.getUsername());
		attributes.put(ConsulDiscoveryCenter.HEALTH_CHECK_PASSWORD, healthCheck.getPassword());
		if (discoveryCenter.addProvider(path, value, attributes)) {
			logger.info("add service {} on {}", channelGroup, value);
		}
		hashedWheelTimer = new HashedWheelTimer();
		timeout = hashedWheelTimer.newTimeout(new ProviderTimeoutTask(path, value), checkTime, TimeUnit.MILLISECONDS);
	}

	@Override
	public void prepareDestroy() throws Throwable {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		String channelGroup = dawdlerContext.getDeployName();
		String path = channelGroup;
		String value = dawdlerContext.getHost() + ":" + dawdlerContext.getPort();
		if (timeout != null) {
			timeout.cancel();
		}
		if (hashedWheelTimer != null) {
			hashedWheelTimer.stop();
		}
		if (discoveryCenter != null) {
			discoveryCenter.deleteProvider(path, value);
			discoveryCenter.destroy();
		}
	}

	public class ProviderTimeoutTask implements TimerTask {
		private String path;
		private String value;

		public ProviderTimeoutTask(String path, String value) {
			this.path = path;
			this.value = value;
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			try {
				if (!discoveryCenter.isExist(path, value)) {
					discoveryCenter.addProvider(path, value, null);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			ConsulLifeCycle.this.timeout = timeout.timer().newTimeout(this, checkTime, TimeUnit.MILLISECONDS);
		}

	}

}
