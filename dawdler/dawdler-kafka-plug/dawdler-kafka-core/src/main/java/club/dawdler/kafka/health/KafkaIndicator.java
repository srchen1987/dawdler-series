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
package club.dawdler.kafka.health;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;

import club.dawdler.core.health.Health;
import club.dawdler.core.health.Health.Builder;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.kafka.config.KafkaConfigProvider;
import club.dawdler.kafka.consumer.KafkaListenerInit;

/**
 * @author jackson.song
 * @version V1.0
 * Kafka健康指示器，检查Kafka消费者连接状态
 */
public class KafkaIndicator implements HealthIndicator {

	@Override
	public String name() {
		return "kafka";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		Map<String, KafkaConsumer<String, byte[]>> consumers = KafkaListenerInit.getConsumers();
		Set<Entry<String, KafkaConsumer<String, byte[]>>> entrySet = consumers.entrySet();
		
		for (Entry<String, KafkaConsumer<String, byte[]>> entry : entrySet) {
			String key = entry.getKey();
			KafkaConsumer<String, byte[]> consumer = entry.getValue();
			Builder childBuilder = Health.up();
			
			try {
				// 获取集群信息来验证连接
				consumer.listTopics(java.time.Duration.ofMillis(1000));
				childBuilder.withDetail("status", "connected");
				// 从KafkaConfigProvider获取配置
				String fileName = key.substring(key.lastIndexOf(".") + 1, key.indexOf("@"));
				String bootstrapServers = KafkaConfigProvider.getInstance(fileName).getBootstrapServers();
				childBuilder.withDetail("bootstrap.servers", bootstrapServers);
				builder.withDetail(key, childBuilder.build().getData());
			} catch (KafkaException e) {
				childBuilder = Health.down();
				childBuilder.withDetail("status", "disconnected");
				childBuilder.withDetail("error", e.getMessage());
				builder.withDetail(key, childBuilder.build().getData());
			} catch (Exception e) {
				childBuilder = Health.down();
				childBuilder.withDetail("status", "error");
				childBuilder.withDetail("error", e.getMessage());
				builder.withDetail(key, childBuilder.build().getData());
			}
		}

		return builder.build();
	}
}