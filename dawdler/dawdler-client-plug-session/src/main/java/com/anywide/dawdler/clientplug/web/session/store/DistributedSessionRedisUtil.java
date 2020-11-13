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
package com.anywide.dawdler.clientplug.web.session.store;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.util.DawdlerTool;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolAbstract;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
/**
 * 
 * @Title:  DistributedSessionRedisUtil.java
 * @Description:  redis操作类
 * @author: jackson.song    
 * @date:   2016年6月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public final class DistributedSessionRedisUtil {
	private static Logger logger = LoggerFactory.getLogger(DistributedSessionRedisUtil.class);
  private static JedisPoolAbstract jedisPool = null;
    
    
    /**
     * 初始化Redis连接池
     */
	private static Properties ps = new Properties();
	static {
	
		String path = DawdlerTool.getcurrentPath() + "redis.properties";
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(path);
			ps.load(inStream);
			String addr = ps.get("addr").toString();
			String auth = ps.get("auth").toString();
			int port = Integer.parseInt(ps.get("port").toString());
			int database = getIfNullReturnDefaultValueInt("database", 0);
			int max_idle = getIfNullReturnDefaultValueInt("max_idle",JedisPoolConfig.DEFAULT_MAX_IDLE); 
			long max_wait = getIfNullReturnDefaultValueLong("max_wait",JedisPoolConfig.DEFAULT_MAX_WAIT_MILLIS);
			int max_active = getIfNullReturnDefaultValueInt("max_active",JedisPoolConfig.DEFAULT_MAX_TOTAL);
			int timeout = getIfNullReturnDefaultValueInt("timeout",Protocol.DEFAULT_TIMEOUT); 
			Object test_on_borrowObj = ps.get("test_on_borrow");
			boolean test_on_borrow = JedisPoolConfig.DEFAULT_TEST_ON_BORROW;
			if(test_on_borrowObj!=null) {
				 test_on_borrow = Boolean.parseBoolean(test_on_borrowObj.toString());
			}
		
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(max_active);
			poolConfig.setMaxIdle(max_idle);
			poolConfig.setMaxWaitMillis(max_wait);
			poolConfig.setTestOnBorrow(test_on_borrow);
			String masterName = (String) ps.get("masterName");
			String sentinels = (String) ps.get("sentinels");
			if(masterName != null && sentinels!=null) {
				String[] sentinelsArray = sentinels.split(",");
				Set<String> sentinelsSet = Arrays.stream(sentinelsArray).collect(Collectors.toSet());
				jedisPool = new JedisSentinelPool(masterName.toString(),sentinelsSet,
						poolConfig,
						 timeout, auth,
							   database);
			}else {
				jedisPool = new JedisPool(poolConfig, addr, port, timeout, auth, database);
			}
		} catch (Exception e) {
			logger.error("",e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	public static int getIfNullReturnDefaultValueInt(String key,int defaultValue) {
		Object value = ps.get(key);
		if(value != null) {
			try {
				return Integer.parseInt(value.toString());
			} catch (Exception e) {
				logger.error("{} use default value : {} case : {}",key,defaultValue,e);
			}
			
		}
		return defaultValue;
	}
	
	public static long getIfNullReturnDefaultValueLong(String key,long defaultValue) {
		Object value = ps.get(key);
		if(value != null) {
			try {
				return Long.parseLong(value.toString());
			} catch (Exception e) {
				logger.error("{} use default value : {} case : {}",key,defaultValue,e);
			}
		}
		return defaultValue;
	}
    
	public static JedisPoolAbstract getJedisPool() {
		return jedisPool;
	}
	
	
	
	
}