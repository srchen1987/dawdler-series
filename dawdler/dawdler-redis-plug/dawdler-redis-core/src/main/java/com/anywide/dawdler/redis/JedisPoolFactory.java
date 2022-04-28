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
package com.anywide.dawdler.redis;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.anywide.dawdler.util.PropertiesUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisPoolFactory.java
 * @Description jedispool jedis 连接池工厂 支持多个池
 * @date 2021年6月18日
 * @email suxuan696@gmail.com
 */
public final class JedisPoolFactory {
	private static Map<String, Pool<Jedis>> pools = new ConcurrentHashMap<>();
	private static AtomicBoolean stopped = new AtomicBoolean(false);

	private static Pool<Jedis> createJedisPool(String fileName) throws Exception {
		Pool<Jedis> jedisPool = null;
		Properties ps = PropertiesUtil.loadActiveProfileIfNotExistUseDefaultProperties(fileName);
		String auth = ps.getProperty("auth");
		String userName = ps.getProperty("userName");
		int database = PropertiesUtil.getIfNullReturnDefaultValueInt("database", 0, ps);
//		
		int minIdle = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.minIdle", JedisPoolConfig.DEFAULT_MIN_IDLE,
				ps);
		int maxIdle = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxIdle", JedisPoolConfig.DEFAULT_MAX_IDLE,
				ps);
		long maxWaitMillis = PropertiesUtil.getIfNullReturnDefaultValueLong("pool.maxWaitMillis",
				JedisPoolConfig.DEFAULT_MAX_WAIT_MILLIS, ps);
		int maxTotal = PropertiesUtil.getIfNullReturnDefaultValueInt("pool.maxTotal", JedisPoolConfig.DEFAULT_MAX_TOTAL,
				ps);
		int timeout = PropertiesUtil.getIfNullReturnDefaultValueInt("timeout", Protocol.DEFAULT_TIMEOUT, ps);
		boolean testOnBorrow = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnBorrow",
				JedisPoolConfig.DEFAULT_TEST_ON_BORROW, ps);
		boolean testOnCreate = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnCreate",
				JedisPoolConfig.DEFAULT_TEST_ON_CREATE, ps);
		boolean testOnReturn = PropertiesUtil.getIfNullReturnDefaultValueBoolean("pool.testOnReturn",
				JedisPoolConfig.DEFAULT_TEST_ON_RETURN, ps);

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWaitMillis(maxWaitMillis);
		poolConfig.setTestOnBorrow(testOnBorrow);
		poolConfig.setTestOnCreate(testOnCreate);
		poolConfig.setTestOnReturn(testOnReturn);
		String masterName = (String) ps.get("masterName");
		String sentinels = (String) ps.get("sentinels");
		if (masterName != null && sentinels != null) {
			String[] sentinelsArray = sentinels.split(",");
			Set<String> sentinelsSet = Arrays.stream(sentinelsArray).collect(Collectors.toSet());
			if (userName != null) {
				jedisPool = new JedisSentinelPool(masterName, sentinelsSet, poolConfig, timeout, userName, auth,
						database);
			} else {
				jedisPool = new JedisSentinelPool(masterName, sentinelsSet, poolConfig, timeout, auth, database);
			}
		} else {
			String addr = ps.getProperty("addr");
			int port = PropertiesUtil.getIfNullReturnDefaultValueInt("port", 5672, ps);
			if (userName != null) {
				jedisPool = new JedisPool(poolConfig, addr, port, timeout, userName, auth, database);
			} else {
				jedisPool = new JedisPool(poolConfig, addr, port, timeout, auth, database);
			}
		}
		return jedisPool;

	}

	public static Pool<Jedis> getJedisPool(String fileName) throws Exception {
		Pool<Jedis> pool = pools.get(fileName);
		if (pool != null)
			return pool;
		if (pool == null) {
			synchronized (pools) {
				pool = pools.get(fileName);
				if (pool == null) {
					pools.put(fileName, createJedisPool(fileName));
				}
			}
		}
		return pools.get(fileName);
	}

	public static void shutdownAll() {
		if (stopped.compareAndSet(false, true)) {
			pools.forEach((k, v) -> {
				if (!v.isClosed()) {
					v.close();
				}
			});
		}
	}

}