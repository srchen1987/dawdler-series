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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;

/**
 * @author jackson.song
 * @version V1.0
 * rabbitmq消息提供者
 */
public class RabbitProvider {
	private static final Logger logger = LoggerFactory.getLogger(RabbitProvider.class);
	private Map<Long, LocalCacheMessage> localCacheMessages = new ConcurrentHashMap<>();
	private AMQPConnectionFactory connectionFactory;

	public RabbitProvider(String fileName) throws Exception {
		connectionFactory = AMQPConnectionFactory.getInstance(fileName);
	}

	public void publish(String exchange, String routingKey, BasicProperties props, byte[] body) throws Exception {
		Connection con = null;
		Channel channel = null;
		try {
			con = connectionFactory.getConnection();
			channel = con.createChannel();
			channel.basicPublish(exchange, routingKey, props, body);
		} finally {
			if (channel != null) {
				channel.close();
			}
			if (con != null) {
				con.close();
			}
		}
	}

	public void publish(String exchange, String routingKey, BasicProperties props, byte[] body,
			ConfirmListener listener) throws Exception {
		Connection con = null;
		Channel channel = null;
		try {
			con = connectionFactory.getConnection();
			channel = con.createChannel();
			channel.basicPublish(exchange, routingKey, props, body);
			channel.addConfirmListener(listener);
		} finally {
			if (channel != null) {
				channel.close();
			}
			if (con != null) {
				con.close();
			}
		}
	}

	/**
	 * @author jackson.song
	 * 推送支持失败重试(发送到mq后没有获取到ack而获取到了nack这种情况)
	 *              (注意要在配置文件中开启confirmSelect=true)
	 * @param exchange
	 * @param routingKey
	 * @param props
	 * @param body
	 * @throws Exception
	 *
	 * 
	 */
	public void publishIfFaildRetry(String exchange, String routingKey, BasicProperties props, byte[] body)
			throws Exception {
		Connection con = null;
		Channel channel = null;
		try {
			con = connectionFactory.getConnection();
			channel = con.createChannel();
			long deliveryTag = channel.getNextPublishSeqNo();
			channel.basicPublish(exchange, routingKey, props, body);
			LocalCacheMessage message = new LocalCacheMessage(deliveryTag, exchange, routingKey, props, body);
			localCacheMessages.put(deliveryTag, message);
			ConfirmListener listener = new ConfirmListener() {

				@Override
				public void handleNack(long deliveryTag, boolean multiple) throws IOException {
					LocalCacheMessage message = localCacheMessages.get(deliveryTag);
					if (message != null) {
						try {
							publishIfFaildRetry(message.getExchange(), message.getRoutingKey(), message.getProps(),
									message.getBody());
						} catch (Exception e) {
							logger.error("", e);
						}
						localCacheMessages.remove(deliveryTag);
					}
				}

				@Override
				public void handleAck(long deliveryTag, boolean multiple) throws IOException {
					if (multiple) {
						localCacheMessages.clear();
					} else {
						localCacheMessages.remove(deliveryTag);
					}
				}
			};
			channel.addConfirmListener(listener);
		} finally {
			if (channel != null) {
				channel.close();
			}
			if (con != null) {
				con.close();
			}
		}
	}

	/**
	 * @author jackson.song
	 * 提供获取Connection的方法用于开启事务功能 通过Connection获取Channal 伪代码:
	 * 	try {
				channel = con.createChannel();
				channel.txSelect();
				channel.basicPublish(exchange, routingKey, props, body);
				channel.txCommit();
		}catch(Exception e) {
				channel.txRollback();
		}finally {
				channel.close();
				con.close();
		}
	 * @throws Exception
	 *
	 * 
	 */
	public Connection getConnection() throws Exception {
		return connectionFactory.getConnection();
	}
	
	/**
	 * 加入本地缓存表 这种情况会引发 顺序问题，如果对queue有顺序要求的 不能采用此方式来处理 特别是两个queue间隔比较短的
	 */
	public static class LocalCacheMessage {
		private long messageId;

		private String exchange;

		private String routingKey;

		private BasicProperties props;

		private byte[] body;

		public LocalCacheMessage(long messageId, String exchange, String routingKey, BasicProperties props,
				byte[] body) {
			this.messageId = messageId;
			this.exchange = exchange;
			this.routingKey = routingKey;
			this.props = props;
			this.body = body;
		}

		public long getMessageId() {
			return messageId;
		}

		public String getRoutingKey() {
			return routingKey;
		}

		public BasicProperties getProps() {
			return props;
		}

		public byte[] getBody() {
			return body;
		}

		public String getExchange() {
			return exchange;
		}

	}

}
