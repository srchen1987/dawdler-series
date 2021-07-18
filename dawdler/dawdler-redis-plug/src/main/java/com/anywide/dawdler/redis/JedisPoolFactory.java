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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.util.DawdlerTool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisPoolF.java
 * @Description jedispool 改变之前的单个jedis的配置 支持多个
 * @date 2021年6月18日
 * @email suxuan696@gmail.com
 */
public final class JedisPoolFactory {
	private static final Logger logger = LoggerFactory.getLogger(JedisPoolFactory.class);
	public static Map<String, Pool<Jedis>> pools = new ConcurrentHashMap<>();

	private static Pool<Jedis> createJedisPool(String propertyFileName) throws Exception {
		Pool<Jedis> jedisPool = null;
		String path = DawdlerTool.getcurrentPath() + propertyFileName + ".properties";
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(path);
			Properties ps = new Properties();
			ps.load(inStream);
			String addr = ps.get("addr").toString();
			String auth = ps.get("auth").toString();
			int port = Integer.parseInt(ps.get("port").toString());
			int database = getIfNullReturnDefaultValueInt("database", 0, ps);
			int max_idle = getIfNullReturnDefaultValueInt("max_idle", JedisPoolConfig.DEFAULT_MAX_IDLE, ps);
			long max_wait = getIfNullReturnDefaultValueLong("max_wait", JedisPoolConfig.DEFAULT_MAX_WAIT_MILLIS, ps);
			int max_active = getIfNullReturnDefaultValueInt("max_active", JedisPoolConfig.DEFAULT_MAX_TOTAL, ps);
			int timeout = getIfNullReturnDefaultValueInt("timeout", Protocol.DEFAULT_TIMEOUT, ps);
			Object test_on_borrowObj = ps.get("test_on_borrow");
			boolean test_on_borrow = JedisPoolConfig.DEFAULT_TEST_ON_BORROW;
			if (test_on_borrowObj != null) {
				test_on_borrow = Boolean.parseBoolean(test_on_borrowObj.toString());
			}

			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(max_active);
			poolConfig.setMaxIdle(max_idle);
			poolConfig.setMaxWaitMillis(max_wait);
			poolConfig.setTestOnBorrow(test_on_borrow);
			String masterName = (String) ps.get("masterName");
			String sentinels = (String) ps.get("sentinels");
			if (masterName != null && sentinels != null) {
				String[] sentinelsArray = sentinels.split(",");
				Set<String> sentinelsSet = Arrays.stream(sentinelsArray).collect(Collectors.toSet());
				jedisPool = new JedisSentinelPool(masterName, sentinelsSet, poolConfig, timeout, auth, database);
			} else {
				jedisPool = new JedisPool(poolConfig, addr, port, timeout, auth, database);
			}
			return jedisPool;
		} catch (Exception e) {
			throw e;
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
				}
			}
		}

	}

	public static int getIfNullReturnDefaultValueInt(String key, int defaultValue, Properties ps) {
		Object value = ps.get(key);
		if (value != null) {
			try {
				return Integer.parseInt(value.toString());
			} catch (Exception e) {
				logger.error("{} use default value : {} case : {}", key, defaultValue, e);
			}

		}
		return defaultValue;
	}

	public static long getIfNullReturnDefaultValueLong(String key, long defaultValue, Properties ps) {
		Object value = ps.get(key);
		if (value != null) {
			try {
				return Long.parseLong(value.toString());
			} catch (Exception e) {
				logger.error("{} use default value : {} case : {}", key, defaultValue, e);
			}
		}
		return defaultValue;
	}

	public static Pool<Jedis> getJedisPool(String propertyFileName) throws Exception {
		Pool<Jedis> pool = pools.get(propertyFileName);
		if (pool == null) {
			pool = createJedisPool(propertyFileName);
			Pool<Jedis> prePool = pools.putIfAbsent(propertyFileName, pool);
			if (prePool != null) {
				pool.close();
				pool = prePool;
			}
		}
		return pool;
	}

}