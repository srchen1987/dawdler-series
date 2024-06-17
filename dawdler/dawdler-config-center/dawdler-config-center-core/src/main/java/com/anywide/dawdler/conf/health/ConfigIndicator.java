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
package com.anywide.dawdler.conf.health;

import java.util.List;

import com.anywide.dawdler.conf.client.ConfigClient;
import com.anywide.dawdler.conf.init.ConfigInit;
import com.anywide.dawdler.core.health.Health;
import com.anywide.dawdler.core.health.Health.Builder;
import com.anywide.dawdler.core.health.HealthIndicator;

/**
 * @author jackson.song
 * @version V1.0
 *          ConfigIndicator 配置中心健康指示器
 */
public class ConfigIndicator implements HealthIndicator {

	@Override
	public String name() {
		return "config";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		ConfigInit configInit = ConfigInit.getInstance();
		List<ConfigClient> configClients = configInit.getConfigClients();
		if (configClients != null) {
			for (ConfigClient configClient : configClients) {
				String key = configClient.type();
				Builder childBuilder = Health.up();
				String info = configClient.info();
				childBuilder.withDetail("info", info);
				builder.withDetail(key, childBuilder.build().getData());
			}
		}
		return builder.build();

	}

}
