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

import redis.clients.jedis.UnifiedJedis;

/**
 * @author jackson.song
 * @version V1.0
 * UnifiedJedis包装类
 */
public class UnifiedJedisWarpper {
	private UnifiedJedis unifiedJedis;
	private int database;
	private int failoverTryCount = 0;
	private int failoverIntervalMillis = 5000;

	public UnifiedJedisWarpper(UnifiedJedis unifiedJedis, int database, int failoverTryCount, int failoverIntervalMillis) {
		this.unifiedJedis = unifiedJedis;
		this.database = database;
		this.failoverTryCount = failoverTryCount;
		this.failoverIntervalMillis = failoverIntervalMillis;
	}

	public UnifiedJedis getUnifiedJedis() {
		return unifiedJedis;
	}

	public int getDatabase() {
		return database;
	}

	public int getFailoverTryCount() {
		return failoverTryCount;
	}

	public int getFailoverInterval() {
		return failoverIntervalMillis;
	}
 


}
