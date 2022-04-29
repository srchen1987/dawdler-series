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
package com.anywide.dawdler.es.restclient.pool.factory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.anywide.dawdler.es.restclient.factory.EsClientFactory;
import com.anywide.dawdler.es.restclient.warpper.ElasticSearchClient;
import com.anywide.dawdler.util.PropertiesUtil;

/**
 *
 * @Title ElasticSearchClientFactory.java
 * @Description ElasticSearchClient多例工厂
 * @author jackson.song
 * @date 2021年11月14日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public class ElasticSearchClientFactory {

	private GenericObjectPool<ElasticSearchClient> genericObjectPool;
	private static Map<String, ElasticSearchClientFactory> instances = new ConcurrentHashMap<>();
	private static AtomicBoolean stopped = new AtomicBoolean(false);

	public static ElasticSearchClientFactory getInstance(String fileName) throws IOException {
		ElasticSearchClientFactory connectionFactory = instances.get(fileName);
		if (connectionFactory != null)
			return connectionFactory;
		synchronized (instances) {
			connectionFactory = instances.get(fileName);
			if (connectionFactory == null) {
				instances.put(fileName, new ElasticSearchClientFactory(fileName));
			}
		}
		return instances.get(fileName);
	}

	public ElasticSearchClientFactory(String fileName) throws IOException {
		Properties ps = PropertiesUtil.loadActiveProfileIfNotExistUseDefaultProperties(fileName);
		String username = ps.getProperty("username", "");
		String password = ps.getProperty("password", "");
		String hosts = ps.getProperty("hosts");
		int connectionRequestTimeout = PropertiesUtil.getIfNullReturnDefaultValueInt("connectionRequestTimeout", -1,
				ps);
		int connectTimeout = PropertiesUtil.getIfNullReturnDefaultValueInt("connectTimeout", -1, ps);
		int socketTimeout = PropertiesUtil.getIfNullReturnDefaultValueInt("socketTimeout", -1, ps);
		GenericObjectPoolConfig<ElasticSearchClient> config = new GenericObjectPoolConfig<>();
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
		EsClientFactory esClientFactory = new EsClientFactory(username, password, hosts, connectionRequestTimeout,
				connectTimeout, socketTimeout);
		PooledEsClientFactory pooledConnectionFactory = new PooledEsClientFactory(esClientFactory);
		genericObjectPool = new GenericObjectPool<ElasticSearchClient>(pooledConnectionFactory, config);
		pooledConnectionFactory.setGenericObjectPool(genericObjectPool);
	}

	public ElasticSearchClient getElasticSearchClient() throws Exception {
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
}
