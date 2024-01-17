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
package com.anywide.dawdler.core.discovery.consul.health;

import com.anywide.dawdler.core.discovery.consul.ConsulDiscoveryCenter;
import com.anywide.dawdler.core.health.Health;
import com.anywide.dawdler.core.health.Health.Builder;
import com.anywide.dawdler.core.health.HealthIndicator;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConsulIndicator.java
 * @Description ConsulIndicator 配置中心健康指示器
 * @date 2023年3月4日
 * @email suxuan696@gmail.com
 */
public class ConsulIndicator implements HealthIndicator {

	@Override
	public String name() {
		return "consul";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		ConsulDiscoveryCenter consulDiscoveryCenter = ConsulDiscoveryCenter.getInstance();
		try {
			builder.withDetail("info", consulDiscoveryCenter.info());
		} catch (Exception e) {
			throw e;
		}
		return builder.build();
	}

}
