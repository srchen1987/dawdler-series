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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.distributed.transaction.message.MessageSender;
import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AMQPSender.java
 * @Description amqp发送者
 * @date 2021年4月11日
 * @email suxuan696@gmail.com
 */
public class AMQPSender implements MessageSender {
	private static final Logger logger = LoggerFactory.getLogger(AMQPSender.class);
	private String exchange = "";
	private AMQPConnectionFactory connectionFactory;

	public AMQPSender() {
		connectionFactory = DistributedTransactionAMQPConnectionFactoryProvider.getInstance().getConnectionFactory();
	}

	public void sent(final String msg) {
		Connection con = null;
		Channel channel = null;
		try {
			con = connectionFactory.getConnection();
			channel = con.createChannel();
			channel.basicPublish(exchange, QUEUE_NAME, null, msg.getBytes());
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException | TimeoutException e) {
					logger.error("", e);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}

	}
}
