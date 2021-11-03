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

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.anywide.dawdler.rabbitmq.connection.pool.ConnectionPool;
import com.anywide.dawdler.util.PropertiesUtil;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 *
 * @Title AMQPConnectionFactory.java
 * @Description AMQP连接工厂
 * @author jackson.song
 * @date 2021年4月11日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public class AMQPConnectionFactory {
	private ConnectionPool connectionPool;
	private static Map<String, AMQPConnectionFactory> instances = new ConcurrentHashMap<>();

	public static AMQPConnectionFactory getInstance(String fileName) throws IOException {
		AMQPConnectionFactory connectionFactory = instances.get(fileName);
		if (connectionFactory != null)
			return connectionFactory;
		synchronized (instances) {
			connectionFactory = instances.get(fileName);
			if (connectionFactory == null) {
				instances.put(fileName, new AMQPConnectionFactory(fileName));
			}
		}
		return instances.get(fileName);
	}

	public AMQPConnectionFactory(String fileName) throws IOException {
		Properties ps = PropertiesUtil.loadActiveProfileIfNotExistUseDefaultProperties(fileName);
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setHost(ps.getProperty("host"));
		connectionFactory.setPort(PropertiesUtil.getIfNullReturnDefaultValueInt("port", 5672, ps));
		connectionFactory.setVirtualHost(ps.getProperty("virtualHost", "/"));
		connectionFactory.setUsername(ps.getProperty("username"));
		connectionFactory.setPassword(ps.getProperty("password"));
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setNetworkRecoveryInterval(
				PropertiesUtil.getIfNullReturnDefaultValueInt("networkRecoveryInterval", 3000, ps));

		int channelSize = PropertiesUtil.getIfNullReturnDefaultValueInt("channel.size", 15, ps);
		int getChannelTimeOut = PropertiesUtil.getIfNullReturnDefaultValueInt("channel.getTimeOut", 15000, ps);

		PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(connectionFactory, channelSize,
				getChannelTimeOut);
		GenericObjectPoolConfig<Connection> config = new GenericObjectPoolConfig<>();
		connectionPool = new ConnectionPool(pooledConnectionFactory, config);
		config.setMaxTotal(PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxTotal", 32, ps));
		config.setMaxWaitMillis(PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxWaitMillis", 5000, ps));
		config.setMinIdle(PropertiesUtil.getIfNullReturnDefaultValueInt("pool.minIdle", 0, ps));
		config.setMaxIdle(PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxIdle", 4, ps));
		boolean testOnBorrow = PropertiesUtil.getIfNullReturnDefaultValueBoolean("testOnBorrow", false, ps);
		boolean testOnCreate = PropertiesUtil.getIfNullReturnDefaultValueBoolean("testOnCreate", false, ps);
		boolean testOnReturn = PropertiesUtil.getIfNullReturnDefaultValueBoolean("testOnReturn", false, ps);
		
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnCreate(testOnCreate);
		config.setTestOnReturn(testOnReturn);
		pooledConnectionFactory.setConnectionPool(connectionPool);
	}

	public Connection getConnection() throws Exception {
		return connectionPool.borrowObject();
	}

	public void close() {
		connectionPool.close();
	}

}
