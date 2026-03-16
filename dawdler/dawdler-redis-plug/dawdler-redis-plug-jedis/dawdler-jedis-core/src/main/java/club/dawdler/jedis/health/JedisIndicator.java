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
package club.dawdler.jedis.health;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import club.dawdler.core.health.Health;
import club.dawdler.core.health.Health.Builder;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.jedis.UnifiedJedisFactory;
import club.dawdler.jedis.UnifiedJedisWarpper;

/**
 * @author jackson.song
 * @version V1.0
 *          JedisIndicator jedis健康指示器
 */
public class JedisIndicator implements HealthIndicator {

	@Override
	public String name() {
		return "jedis";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		Map<String, UnifiedJedisWarpper> unifiedJedisCache = UnifiedJedisFactory.getUnifiedJedisCache();
		Set<Map.Entry<String, UnifiedJedisWarpper>> entrySet = unifiedJedisCache.entrySet();
		for (Entry<String, UnifiedJedisWarpper> entry : entrySet) {
			String key = entry.getKey();
			UnifiedJedisWarpper unifiedJedisWarpper = entry.getValue();
			Builder childBuilder = Health.up();
			String version = unifiedJedisWarpper.getUnifiedJedis().info("Server").split("\r\n")[1];
				childBuilder.withDetail("version", version);
				builder.withDetail(key, childBuilder.build().getData());
		}
		return builder.build();
	}

}
