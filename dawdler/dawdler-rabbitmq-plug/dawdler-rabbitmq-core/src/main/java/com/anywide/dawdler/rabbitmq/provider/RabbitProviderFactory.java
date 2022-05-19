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
package com.anywide.dawdler.rabbitmq.provider;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.rabbitmq.provider.annotation.RabbitInjector;

/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title RabbitProviderFactory.java
 * @Description rabbitmq消息提供者工厂
 * @date 2022年4月15日
 * @email suxuan696@gmail.com
 */
public class RabbitProviderFactory {
	private static Map<String, RabbitProvider> providers = new ConcurrentHashMap<>();

	public static RabbitProvider getRabbitProvider(String fileName) throws Exception {
		RabbitProvider provider = providers.get(fileName);
		if (provider != null) {
			return provider;
		}
		synchronized (providers) {
			provider = providers.get(fileName);
			if (provider == null) {
				providers.put(fileName, new RabbitProvider(fileName));
			}
		}
		return providers.get(fileName);
	}

	public static void initField(Object target, Class<?> clazz)
			throws IllegalArgumentException, IllegalAccessException, Exception {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			RabbitInjector rabbitInjector = field.getAnnotation(RabbitInjector.class);
			if (!field.getType().isPrimitive()) {
				Class<?> serviceClass = field.getType();
				if (rabbitInjector != null && RabbitProvider.class.isAssignableFrom(serviceClass)) {
					field.setAccessible(true);
					field.set(target, RabbitProviderFactory.getRabbitProvider(rabbitInjector.value()));
				}
			}
		}
	}

}
