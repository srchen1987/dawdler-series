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
package com.anywide.dawdler.jedis.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisDistributedLockHolder
 * @Description JedisDistributedLock持有者
 * @date 2023年7月18日
 * @email suxuan696@gmail.com
 */
public class JedisDistributedLockHolder {
	private int intervalInMillis; // 下一次重试等待，单位毫秒
	public static final int DEFAULT_INTERVAL_IN_MILLIS = 20;
	private Pool<Jedis> jedisPool;
	private long lockExpiryInMillis; // 锁的过期时长，单位毫秒
	public static final long DEFAULT_LOCK_EXPIRY_IN_MILLIS = 3000;
	private boolean useWatchDog;

	public JedisDistributedLockHolder(Pool<Jedis> jedisPool, long lockExpiryInMillis, int intervalInMillis,
			boolean useWatchDog) {
		this.jedisPool = jedisPool;
		this.lockExpiryInMillis = lockExpiryInMillis;
		this.intervalInMillis = intervalInMillis;
		this.useWatchDog = useWatchDog;
	}

	public JedisDistributedLock createLock(String lockKey) {
		return new JedisDistributedLock(jedisPool, lockKey, lockExpiryInMillis, intervalInMillis, useWatchDog);
	}
}
