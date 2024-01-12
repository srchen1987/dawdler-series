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
package com.anywide.dawdler.rabbitmq.health;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.anywide.dawdler.core.health.Health;
import com.anywide.dawdler.core.health.Health.Builder;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;
import com.rabbitmq.client.Connection;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RabbitIndicator.java
 * @Description RabbitIndicator rabbitmq健康指示器
 * @date 2022年5月1日
 * @email suxuan696@gmail.com
 */
public class RabbitIndicator implements HealthIndicator {

	@Override
	public String name() {
		return "rabbit";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		Map<String, AMQPConnectionFactory> instances = AMQPConnectionFactory.getInstances();
		Set<Map.Entry<String, AMQPConnectionFactory>> entrySet = instances.entrySet();
		for (Entry<String, AMQPConnectionFactory> entry : entrySet) {
			String key = entry.getKey();
			AMQPConnectionFactory factory = entry.getValue();
			Connection con = null;
			Builder childBuilder = Health.up();
			try {
				con = factory.getConnection();
				String version = con.getServerProperties().get("version").toString();
				childBuilder.withDetail("version", version);
				builder.withDetail(key, childBuilder.build().getData());
			} catch (Exception e) {
				throw e;
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (IOException e) {
					}
				}
			}
		}

		return builder.build();
	}

}
