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

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.rabbitmq.connection.AMQPConnectionWarpper;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection;

/**
 *
 * @Title PooledConnectionFactory.java
 * @Description 连接池工厂 通过pool2实现
 * @author jackson.song
 * @date 2021年4月11日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public class PooledConnectionFactory extends BasePooledObjectFactory<Connection> {
	private static final Logger logger = LoggerFactory.getLogger(PooledConnectionFactory.class);

	private ConnectionFactory connectionFactory;
	private GenericObjectPool<Connection> genericObjectPool;
	private int channelSize;
	private int getChannelTimeOut;
	private boolean confirmSelect;

	@Override
	public boolean validateObject(PooledObject<Connection> p) {
		return p.getObject().isOpen();
	}

	public PooledConnectionFactory(ConnectionFactory connectionFactory, int channelSize, int getChannelTimeOut,
			boolean confirmSelect) {
		this.connectionFactory = connectionFactory;
		this.channelSize = channelSize;
		this.getChannelTimeOut = getChannelTimeOut;
		this.confirmSelect = confirmSelect;
	}

	public void setConnectionPool(GenericObjectPool<Connection> genericObjectPool) {
		this.genericObjectPool = genericObjectPool;
	}

	public void setChannelSize(int channelSize) {
		this.channelSize = channelSize;
	}

	public void setGetChannelTimeOut(int getChannelTimeOut) {
		this.getChannelTimeOut = getChannelTimeOut;
	}

	@Override
	public Connection create() throws Exception {

		AutorecoveringConnection con = (AutorecoveringConnection) connectionFactory.newConnection();
		con.addShutdownListener((shutdownSignalException) -> {
//			logger.error("amqp connection shutdown:", shutdownSignalException);
			try {
				genericObjectPool.invalidateObject((Connection) shutdownSignalException.getReference());
			} catch (Exception e) {
			}
		});

		con.addRecoveryListener(new RecoveryListener() {

			@Override
			public void handleRecoveryStarted(Recoverable recoverable) {
			}

			@Override
			public void handleRecovery(Recoverable recoverable) {
				logger.info("amqp connection recovery:" + recoverable);
			}
		});
		return new AMQPConnectionWarpper(con, genericObjectPool, channelSize, getChannelTimeOut, confirmSelect);
	}

	@Override
	public void destroyObject(PooledObject<Connection> p) throws Exception {
		((AMQPConnectionWarpper) p.getObject()).physicsClose();
	}

	@Override
	public PooledObject<Connection> wrap(Connection con) {
		return new DefaultPooledObject<>(con);
	}
}
