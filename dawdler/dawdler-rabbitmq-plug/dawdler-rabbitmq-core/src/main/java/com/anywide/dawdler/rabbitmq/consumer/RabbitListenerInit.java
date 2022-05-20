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
package com.anywide.dawdler.rabbitmq.consumer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;
import com.anywide.dawdler.rabbitmq.consumer.annotation.RabbitListener;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 *
 * @Title RabbitListenerInit.java
 * @Description 初始化Listener
 * @author jackson.song
 * @date 2022年4月14日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public class RabbitListenerInit {
	private static final Logger logger = LoggerFactory.getLogger(RabbitListenerInit.class);
	private static Map<String, Object> listenerCache = new ConcurrentHashMap<>();

	public static void initRabbitListener(Object target, Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			RabbitListener listener = method.getAnnotation(RabbitListener.class);
			if (listener != null) {
				String key = method.toGenericString();
				Object object = listenerCache.get(key);
				listenerCache.put(key, target);
				if (object == null) {
					try {
						AMQPConnectionFactory factory = AMQPConnectionFactory.getInstance(listener.fileName());
						Connection con = factory.getConnection();
						Channel channel = con.createChannel();
						channel.basicConsume(listener.queueName(), listener.autoAck(), new DefaultConsumer(channel) {
							public void handleDelivery(String consumerTag, Envelope envelope,
									AMQP.BasicProperties properties, byte[] body) throws IOException {
								Message message = new Message(consumerTag, envelope, properties, body);
								try {
									method.invoke(listenerCache.get(key), message);
								} catch (Throwable e) {
									logger.error("", e.getCause());
									if (listener.retry()) {
										communalRetryMethod(message, channel, listener.retryCount());
									}
								}
								if (!listener.autoAck()) {
									long deliveryTag = envelope.getDeliveryTag(); // autoACK
									channel.basicAck(deliveryTag, false);
								}
							}
						});
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		}

	}

	public static void communalRetryMethod(Message message, Channel channel, int retryCount) throws IOException {
		// 获取该次消息的routingkey
		String routingKey = message.getEnvelope().getRoutingKey();
		if (getRetryCount(message) < retryCount) {
			// 发送到重试队列中
			channel.basicPublish(AMQPConnectionFactory.RABBIT_RETRY_EXCHANGE, routingKey,
					(AMQP.BasicProperties) message.getProperties(), message.getBody());
		} else {
			// 超过指定次数发送到失败队列
			channel.basicPublish(AMQPConnectionFactory.RABBIT_FAIL_EXCHANGE, routingKey,
					(AMQP.BasicProperties) message.getProperties(), message.getBody());
		}
	}

	/**
	 * 获取消息失败次数
	 *
	 * @param message
	 * @return
	 */
	private static long getRetryCount(Message message) {
		Map<String, Object> header = message.getProperties().getHeaders();
		long retryCount = 0L;
		if (header != null && header.containsKey("x-death")) {
			List<Map<String, Object>> deaths = (List<Map<String, Object>>) header.get("x-death");
			if (deaths.size() > 0) {
				Map<String, Object> death = deaths.get(0);
				retryCount = (Long) death.get("count");
			}
		}
		return retryCount;
	}
}
