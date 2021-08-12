package com.anywide.dawdler.rabbitmq.connection.pool.factory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.anywide.dawdler.rabbitmq.connection.AMQPConnectionWarpper;
import com.anywide.dawdler.rabbitmq.connection.pool.ConnectionPool;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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
	private ConnectionFactory connectionFactory;
	private ConnectionPool connectionPool;
	private int channelSize = 16;
	private int getChannelTimeOut = 6000;

	public PooledConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setConnectionPool(ConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}

	public void setChannelSize(int channelSize) {
		this.channelSize = channelSize;
	}

	public void setGetChannelTimeOut(int getChannelTimeOut) {
		this.getChannelTimeOut = getChannelTimeOut;
	}

	@Override
	public Connection create() throws Exception {
		return new AMQPConnectionWarpper(connectionFactory.newConnection(), connectionPool, channelSize,
				getChannelTimeOut);
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
