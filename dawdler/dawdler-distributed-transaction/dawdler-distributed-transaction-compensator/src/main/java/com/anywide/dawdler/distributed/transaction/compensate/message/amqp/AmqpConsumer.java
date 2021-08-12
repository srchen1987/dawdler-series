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
package com.anywide.dawdler.distributed.transaction.compensate.message.amqp;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.distributed.transaction.compensate.message.MessageConsumer;
import com.anywide.dawdler.distributed.transaction.message.MessageSender;
import com.anywide.dawdler.distributed.transaction.message.amqp.rabbitmq.DistributedTransactionAMQPConnectionFactoryProvider;
import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
*
* @Title AmqpConsumer.java
* @Description Amqp消息消费者 将消息分发到不同的处理者上去执行，处理者需要继承DistributedTransactionCustomProcessor
* @author jackson.song
* @date 2021年4月17日
* @version V1.0
* @email suxuan696@gmail.com
*/
public class AmqpConsumer extends MessageConsumer {
	private static Logger logger = LoggerFactory.getLogger(AmqpConsumer.class);
	private AMQPConnectionFactory connectionFactory;
	private Connection con;
	private Channel channel;
	
	@Override
	public void start() throws Exception {
		super.start();
		con = connectionFactory.getConnection();
		channel = con.createChannel();
		consume();
	}

	@Override
	public void shutdown() {
		if(channel != null) {
			try {
				channel.close();
			} catch (IOException | TimeoutException e) {
				logger.error("", e);
			}
		}
		if(con != null) {
			try {
				con.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		super.shutdown();
	}

	public AmqpConsumer() {
		connectionFactory = DistributedTransactionAMQPConnectionFactoryProvider.getInstance().getConnectionFactory();

	}

	public void consume() throws Exception {
		channel.basicConsume(MessageSender.queueName, true, new DefaultConsumer(channel) {
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				consume(body);
//				long deliveryTag = envelope.getDeliveryTag(); //autoACK
//				channel.basicAck(deliveryTag, false);
			}
		});
	}

	

}
