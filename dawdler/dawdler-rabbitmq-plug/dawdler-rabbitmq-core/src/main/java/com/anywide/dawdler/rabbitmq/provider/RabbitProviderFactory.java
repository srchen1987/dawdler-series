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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;
import com.anywide.dawdler.rabbitmq.consumer.Message;
import com.anywide.dawdler.rabbitmq.consumer.annotation.RabbitListener;
import com.anywide.dawdler.rabbitmq.provider.annotation.RabbitInjector;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 *
 * @Title RabbitProviderFactory.java
 * @Description rabbitmq消息提供者工厂
 * @author jackson.song
 * @date 2022年4月15日
 * @version V1.0
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
	
	public static void initField(Object target, Class<?> clazz) throws IllegalArgumentException, IllegalAccessException, Exception {
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
