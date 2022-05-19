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
package com.anywide.dawdler.distributed.transaction.message.amqp.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;

/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title DistributedTransactionAMQPConnectionFactoryProvider.java
 * @Description amqp工厂单例提供者
 * @date 2021年4月11日
 * @email suxuan696@gmail.com
 */
public class DistributedTransactionAMQPConnectionFactoryProvider {
	private static final Logger logger = LoggerFactory.getLogger(AMQPSender.class);
	private AMQPConnectionFactory connectionFactory;

	private DistributedTransactionAMQPConnectionFactoryProvider() {
		try {
			connectionFactory = AMQPConnectionFactory.getInstance("distributed-transaction-rabbitmq");
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public AMQPConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public static DistributedTransactionAMQPConnectionFactoryProvider getInstance() {
		return AMQPConnectionFactoryProviderHolder.instance;
	}

	public static class AMQPConnectionFactoryProviderHolder {
		private static DistributedTransactionAMQPConnectionFactoryProvider instance = new DistributedTransactionAMQPConnectionFactoryProvider();
	}

}
