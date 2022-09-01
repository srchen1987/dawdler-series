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
package com.anywide.dawdler.rabbitmq.connection.pool.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.anywide.dawdler.util.PropertiesUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title AMQPConnectionFactory.java
 * @Description AMQP连接多例工厂
 * @date 2021年4月11日
 * @email suxuan696@gmail.com
 */
public class AMQPConnectionFactory {
	private GenericObjectPool<Connection> genericObjectPool;
	private final static Map<String, AMQPConnectionFactory> instances = new ConcurrentHashMap<>();
	private static AtomicBoolean stopped = new AtomicBoolean(false);
	private int channelSize;

	// 重试交换器
	public final static String RABBIT_RETRY_EXCHANGE = "rabbit_retry_exchange";
	// 重试队列（重试队列会设置过期时间，过期后会再次发送到正常工作队列中）
	private final static String RABBIT_RETRY_QUEUE = "rabbit_retry_queue";
	// 消息重试几次后未成功发送至异常交换器
	public final static String RABBIT_FAIL_EXCHANGE = "rabbit_fail_exchange";
	// 异常信息存储队列
	private final static String RABBIT_FAIL_QUEUE = "rabbit_fail_queue";

	private static boolean useConfig = false;
	static {
		try {
			Class.forName("com.anywide.dawdler.conf.cache.ConfigMappingDataCache");
			useConfig = true;
		} catch (ClassNotFoundException e) {
		}
	}

	public static AMQPConnectionFactory getInstance(String fileName) throws Exception {
		AMQPConnectionFactory connectionFactory = instances.get(fileName);
		if (connectionFactory != null) {
			return connectionFactory;
		}
		synchronized (instances) {
			connectionFactory = instances.get(fileName);
			if (connectionFactory == null) {
				connectionFactory = new AMQPConnectionFactory(fileName);
				instances.put(fileName, connectionFactory);
			}
		}
		return connectionFactory;
	}

	public void initQueue(int ttlTime) throws Exception {
		Connection con = null;
		Channel channel = null;

		try {
			con = getConnection();
			channel = con.createChannel();
			// fanout类型交换器会把消息发送至所有绑定的队列中
			channel.exchangeDeclare(RABBIT_RETRY_EXCHANGE, "fanout", true);
			channel.exchangeDeclare(RABBIT_FAIL_EXCHANGE, "fanout", true);
			Map<String, Object> agreement = new HashMap<>();
			agreement.put("x-dead-letter-exchange", "");
			agreement.put("x-message-ttl", ttlTime);// 设置消息存在队列中的时间(单位秒)，时间过期后消息会发送至默认交换器中
			channel.queueDeclare(RABBIT_RETRY_QUEUE, true, false, false, agreement);
			channel.queueDeclare(RABBIT_FAIL_QUEUE, true, false, false, null);
			channel.queueBind(RABBIT_RETRY_QUEUE, RABBIT_RETRY_EXCHANGE, RABBIT_FAIL_QUEUE);
			channel.queueBind(RABBIT_FAIL_QUEUE, RABBIT_FAIL_EXCHANGE, RABBIT_FAIL_QUEUE);
		} finally {
			if (con != null) {
				con.close();
			}
			if (channel != null) {
				channel.close();
			}
		}

	}

	public AMQPConnectionFactory(String fileName) throws Exception {
		Properties ps = null;
		if (useConfig) {
			try {
				ps = PropertiesUtil.loadActiveProfileIfNotExistUseDefaultProperties(fileName);
			} catch (Exception e) {
				Map<String, Object> attributes = com.anywide.dawdler.conf.cache.ConfigMappingDataCache
						.getMappingDataCache(fileName);
				if (attributes == null) {
					throw e;
				}
				ps = new Properties();
				Set<Entry<String, Object>> entrySet = attributes.entrySet();
				for (Entry<String, Object> entry : entrySet) {
					ps.setProperty(entry.getKey(), entry.getValue().toString());
				}
			}
		} else {
			ps = PropertiesUtil.loadActiveProfileIfNotExistUseDefaultProperties(fileName);
		}

		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(ps.getProperty("host"));
		connectionFactory.setPort(PropertiesUtil.getIfNullReturnDefaultValueInt("port", 5672, ps));
		connectionFactory.setVirtualHost(ps.getProperty("virtualHost", "/"));
		connectionFactory.setUsername(ps.getProperty("username"));
		connectionFactory.setPassword(ps.getProperty("password"));
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setNetworkRecoveryInterval(
				PropertiesUtil.getIfNullReturnDefaultValueInt("networkRecoveryInterval", 3000, ps));
		channelSize = PropertiesUtil.getIfNullReturnDefaultValueInt("channel.size", 16, ps);
		int getChannelTimeOut = PropertiesUtil.getIfNullReturnDefaultValueInt("channel.getTimeOut", 15000, ps);

		boolean confirmSelect = PropertiesUtil.getIfNullReturnDefaultValueBoolean("confirmSelect", true, ps);

		PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(connectionFactory, channelSize,
				getChannelTimeOut, confirmSelect);
		GenericObjectPoolConfig<Connection> config = new GenericObjectPoolConfig<>();
		genericObjectPool = new GenericObjectPool<Connection>(pooledConnectionFactory, config);

		int ttlTime = PropertiesUtil.getIfNullReturnDefaultValueInt("ttlTime", 5000, ps);

		int minIdle = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.minIdle",
				GenericObjectPoolConfig.DEFAULT_MIN_IDLE, ps);
		int maxIdle = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxIdle",
				GenericObjectPoolConfig.DEFAULT_MAX_IDLE, ps);
		long maxWaitMillis = PropertiesUtil.getIfNullReturnDefaultValueLong("pool.maxWaitMillis",
				GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS, ps);
		int maxTotal = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxTotal",
				GenericObjectPoolConfig.DEFAULT_MAX_TOTAL, ps);
		boolean testOnBorrow = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnBorrow",
				GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW, ps);
		boolean testOnCreate = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnCreate",
				GenericObjectPoolConfig.DEFAULT_TEST_ON_CREATE, ps);
		boolean testOnReturn = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnReturn",
				GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN, ps);

		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnCreate(testOnCreate);
		config.setTestOnReturn(testOnReturn);
		pooledConnectionFactory.setConnectionPool(genericObjectPool);

		initQueue(ttlTime);
	}

	public Connection getConnection() throws Exception {
		return genericObjectPool.borrowObject();
	}

	public void close() {
		genericObjectPool.close();
	}

	public boolean isClose() {
		return genericObjectPool.isClosed();
	}

	public static void shutdownAll() {
		if (stopped.compareAndSet(false, true)) {
			instances.forEach((k, v) -> {
				if (!v.isClose()) {
					v.close();
				}
			});
		}
	}

	public int getChannelSize() {
		return channelSize;
	}

	public static Map<String, AMQPConnectionFactory> getInstances() {
		return instances;
	}

}
