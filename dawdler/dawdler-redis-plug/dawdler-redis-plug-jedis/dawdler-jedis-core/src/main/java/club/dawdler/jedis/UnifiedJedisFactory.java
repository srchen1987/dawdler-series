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
package club.dawdler.jedis;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import club.dawdler.util.PropertiesUtil;
import redis.clients.jedis.ClientSetInfoConfig;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.HostAndPortMapper;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.RedisSentinelClient;
import redis.clients.jedis.UnifiedJedis;

/**
 * @author jackson.song
 * @version V1.0
 * jedispool jedis 连接池工厂 支持多个池
 */
public final class UnifiedJedisFactory {
	private static final Map<String, UnifiedJedisWarpper> unifiedJedisCache = new ConcurrentHashMap<>();

	private static AtomicBoolean stopped = new AtomicBoolean(false);

	private static UnifiedJedisWarpper createUnifiedJedis(String fileName) throws Exception {
		UnifiedJedis unifiedJedis = null;
		Properties ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter(fileName);
		String user = ps.getProperty("user");
		String password = ps.getProperty("password");
		String clientName = ps.getProperty("clientName");
		String sentinelUser = ps.getProperty("sentinelUser");
		String sentinelPassword = ps.getProperty("sentinelPassword");
		String sentinelClientName = ps.getProperty("sentinelClientName");
		String suffix = ps.getProperty("suffix", "suffix");
		int failoverTryCount = PropertiesUtil.getIfNullReturnDefaultValueInt("failoverTryCount", 0, ps);
		int failoverIntervalMillis = PropertiesUtil.getIfNullReturnDefaultValueInt("failoverIntervalMillis", 5000, ps);

		int blockingSocketTimeoutMillis = PropertiesUtil.getIfNullReturnDefaultValueInt("blockingSocketTimeout",
				0, ps);
		int database = PropertiesUtil.getIfNullReturnDefaultValueInt("database", 0, ps);

		int minIdle = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.minIdle",
						ConnectionPoolConfig.DEFAULT_MIN_IDLE,
				ps);
		int maxIdle = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxIdle",
						ConnectionPoolConfig.DEFAULT_MAX_IDLE,
				ps);
		long maxWaitMillis = PropertiesUtil.getIfNullReturnDefaultValueLong("pool.maxWaitMillis",
				ConnectionPoolConfig.DEFAULT_MAX_WAIT_MILLIS, ps);
				int maxTotal = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxTotal",
				ConnectionPoolConfig.DEFAULT_MAX_TOTAL,
				ps);
		int timeout = PropertiesUtil.getIfNullReturnDefaultValueInt("timeout", Protocol.DEFAULT_TIMEOUT, ps);
		boolean testOnBorrow = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnBorrow",
				ConnectionPoolConfig.DEFAULT_TEST_ON_BORROW, ps);
		boolean testOnCreate = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnCreate",
				ConnectionPoolConfig.DEFAULT_TEST_ON_CREATE, ps);
		boolean testOnReturn = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnReturn",
				ConnectionPoolConfig.DEFAULT_TEST_ON_RETURN, ps);
		ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWaitMillis(maxWaitMillis);
		poolConfig.setTestOnBorrow(testOnBorrow);
		poolConfig.setTestOnCreate(testOnCreate);
		poolConfig.setTestOnReturn(testOnReturn);
		String masterName = (String) ps.get("masterName");
		String sentinels = (String) ps.get("sentinels");
		ClientSetInfoConfig clientSetInfoConfig = ClientSetInfoConfig.withLibNameSuffix(suffix);
		if (masterName != null && sentinels != null) {
			String[] sentinelsArray = sentinels.split(",");
			Set<HostAndPort> sentinelsSet =  Arrays.stream(sentinelsArray).map(sentinel -> {
				String[] hostAndPort = sentinel.split(":");
				return new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
			}).collect(Collectors.toSet());
			JedisClientConfig sentinelClientConfig = DefaultJedisClientConfig.builder()
					.socketTimeoutMillis(timeout).connectionTimeoutMillis(timeout).
					clientName(sentinelClientName).blockingSocketTimeoutMillis(blockingSocketTimeoutMillis)
					 .user(sentinelUser).password(sentinelPassword)
					.build();
			JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
					.socketTimeoutMillis(timeout).connectionTimeoutMillis(timeout).
					clientName(clientName).clientSetInfoConfig(clientSetInfoConfig).blockingSocketTimeoutMillis(blockingSocketTimeoutMillis)
					.database(database).user(user).password(password)
					.build();
			unifiedJedis = RedisSentinelClient.builder().poolConfig(poolConfig).clientConfig(jedisClientConfig).sentinelClientConfig(sentinelClientConfig).masterName(masterName).sentinels(sentinelsSet).build();
		} else {
			String host = ps.getProperty("host");
			int port = PropertiesUtil.getIfNullReturnDefaultValueInt("port", 5672, ps);
			HostAndPortMapper hostAndPortMapper = (hostAndPort) -> {
				return hostAndPort;
			};
			hostAndPortMapper.getHostAndPort(new HostAndPort(host, port));
			JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
					.hostAndPortMapper(hostAndPortMapper).socketTimeoutMillis(timeout).connectionTimeoutMillis(timeout).
					clientName(clientName).clientSetInfoConfig(clientSetInfoConfig).blockingSocketTimeoutMillis(blockingSocketTimeoutMillis)
					.database(database).user(user).password(password)
					.build();
			unifiedJedis = RedisClient.builder().poolConfig(poolConfig).clientConfig(jedisClientConfig).build();
		}
		return new UnifiedJedisWarpper(unifiedJedis, database, failoverTryCount, failoverIntervalMillis);
	}

	public static UnifiedJedisWarpper getUnifiedJedis(String fileName) throws Exception {
		UnifiedJedisWarpper unifiedJedisWarpper = unifiedJedisCache.get(fileName);
		if (unifiedJedisWarpper != null) {
			return unifiedJedisWarpper;
		}
		synchronized (unifiedJedisCache) {
			unifiedJedisWarpper = unifiedJedisCache.get(fileName);
			if (unifiedJedisWarpper == null) {
				unifiedJedisCache.put(fileName, createUnifiedJedis(fileName));
			}
		}
		return unifiedJedisCache.get(fileName);
	}

	public static void shutdownAll() {
		if (stopped.compareAndSet(false, true)) {
			unifiedJedisCache.forEach((k, v) -> {
					v.getUnifiedJedis().close();
			});
		}
	}

	public static Map<String, UnifiedJedisWarpper> getUnifiedJedisCache() {
		return unifiedJedisCache;
	}

	public static void main(String[] args) {
		String host = "192.168.1.1";
		int port = 6379;
		HostAndPort hostAndPort = new HostAndPort(host, port);
		HostAndPortMapper hostAndPortMapper = (h) -> {
			System.out.println("test:"+h);
			return hostAndPort;
		};
		System.out.println(hostAndPortMapper.getHostAndPort(hostAndPort));
	}
}
