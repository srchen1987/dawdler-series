package com.anywide.dawdler.rabbitmq.connection.pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.rabbitmq.client.Connection;

/**
*
* @Title ConnectionPool.java
* @Description 连接池对象
* @author jackson.song
* @date 2021年4月11日
* @version V1.0
* @email suxuan696@gmail.com
*/
public class ConnectionPool extends GenericObjectPool<Connection> {

	public ConnectionPool(PooledObjectFactory<Connection> factory) {
		super(factory);
	}
	
	public ConnectionPool(PooledObjectFactory<Connection> factory, GenericObjectPoolConfig<Connection> config) {
        super(factory, config);
    }

    public ConnectionPool(PooledObjectFactory<Connection> factory, GenericObjectPoolConfig<Connection> config, AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }

}
