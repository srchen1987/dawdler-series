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
package com.anywide.dawdler.jedis.health;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.anywide.dawdler.core.health.Health;
import com.anywide.dawdler.core.health.Health.Builder;
import com.anywide.dawdler.jedis.JedisPoolFactory;
import com.anywide.dawdler.core.health.HealthIndicator;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title JedisIndicator.java
 * @Description JedisIndicator jedis健康指示器
 * @date 2022年5月1日
 * @email suxuan696@gmail.com
 */
public class JedisIndicator implements HealthIndicator {

	@Override
	public String name() {
		return "jedis";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		Map<String, Pool<Jedis>> pools = JedisPoolFactory.getPools();
		Set<Map.Entry<String, Pool<Jedis>>> entrySet = pools.entrySet();
		for (Entry<String, Pool<Jedis>> entry : entrySet) {
			String key = entry.getKey();
			Pool<Jedis> pool = entry.getValue();
			Jedis jedis = null;
			Builder childBuilder = Health.up();
			try {
				jedis = pool.getResource();
				String version = jedis.info("Server").split("\r\n")[1];
				childBuilder.withDetail("version", version);
				builder.withDetail(key, childBuilder.build().getData());
			} catch (Exception e) {
				throw e;
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		return builder.build();
	}

}
