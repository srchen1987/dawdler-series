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
package club.dawdler.kafka.provider;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.kafka.provider.annotation.KafkaInjector;

/**
 * @author jackson.song
 * @version V1.0
 * kafka消息提供者工厂
 */
public class KafkaProviderFactory {
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderFactory.class);
	private static Map<String, KafkaProvider> providers = new ConcurrentHashMap<>();

	public static KafkaProvider getKafkaProvider(String fileName) throws Exception {
		KafkaProvider provider = providers.get(fileName);
		if (provider != null) {
			return provider;
		}
		synchronized (providers) {
			provider = providers.get(fileName);
			if (provider == null) {
				providers.put(fileName, new KafkaProvider(fileName));
			}
		}
		return providers.get(fileName);
	}

	public static void initField(Object target, Class<?> clazz)
			throws IllegalArgumentException, IllegalAccessException, Exception {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			KafkaInjector kafkaInjector = field.getAnnotation(KafkaInjector.class);
			if (!field.getType().isPrimitive()) {
				Class<?> serviceClass = field.getType();
				if (kafkaInjector != null && KafkaProvider.class.isAssignableFrom(serviceClass)) {
					field.setAccessible(true);
					field.set(target, KafkaProviderFactory.getKafkaProvider(kafkaInjector.value()));
				}
			}
		}
	}
	
	public static void shutdownAll() {
		logger.info("Shutting down all Kafka providers");
		for (KafkaProvider provider : providers.values()) {
			try {
				provider.close();
			} catch (Exception e) {
				logger.error("Error closing Kafka provider", e);
			}
		}
		providers.clear();
		logger.info("All Kafka providers have been shut down");
	}
}