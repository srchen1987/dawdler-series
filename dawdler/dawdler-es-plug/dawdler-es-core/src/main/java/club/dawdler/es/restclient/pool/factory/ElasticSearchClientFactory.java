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
package club.dawdler.es.restclient.pool.factory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import club.dawdler.es.restclient.factory.EsClientFactory;
import club.dawdler.es.restclient.wrapper.ElasticSearchClient;
import club.dawdler.util.PropertiesUtil;

/**
 * @author jackson.song
 * @version V1.0
 * ElasticSearchClient多例工厂
 */
public class ElasticSearchClientFactory {
	private GenericObjectPool<ElasticSearchClient> genericObjectPool;
	private static final Map<String, ElasticSearchClientFactory> INSTANCES = new ConcurrentHashMap<>();
	private static AtomicBoolean stopped = new AtomicBoolean(false);

	public static ElasticSearchClientFactory getInstance(String fileName) throws Exception {
		ElasticSearchClientFactory connectionFactory = INSTANCES.get(fileName);
		if (connectionFactory != null) {
			return connectionFactory;
		}
		synchronized (INSTANCES) {
			connectionFactory = INSTANCES.get(fileName);
			if (connectionFactory == null) {
				INSTANCES.put(fileName, new ElasticSearchClientFactory(fileName));
			}
		}
		return INSTANCES.get(fileName);
	}

	public ElasticSearchClientFactory(String fileName) throws Exception {
		Properties ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter(fileName);
		String username = ps.getProperty("username", "");
		String password = ps.getProperty("password", "");
		String hosts = ps.getProperty("hosts");
		String keystorePath = ps.getProperty("keystorePath");
		String keystorePassword = ps.getProperty("keystorePassword");
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
				connectTimeout, socketTimeout, keystorePath, keystorePassword);
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
			INSTANCES.forEach((k, v) -> {
				if (!v.isClose()) {
					v.close();
				}
			});
		}
	}

	public static Map<String, ElasticSearchClientFactory> getInstances() {
		return INSTANCES;
	}
}
