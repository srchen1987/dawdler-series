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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.anywide.dawdler.jedis.JedisPoolFactory;
import com.anywide.dawdler.jedis.annotation.JedisLockInjector;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisDistributedLockFactory
 * @Description JedisDistributedLock工厂
 * @date 2023年7月18日
 * @email suxuan696@gmail.com
 */
public class JedisDistributedLockHolderFactory {

	private final static Map<String, JedisDistributedLockHolder> JEDIS_DISTRIBUTED_LOCK_HOLDERS = new ConcurrentHashMap<>();

	private static JedisDistributedLockHolder getJedisDistributedLockHolder(JedisLockInjector jedisLockInjector)
			throws Exception {
		String fileName = jedisLockInjector.fileName();
		JedisDistributedLockHolder jedisDistributedLockHolder = JEDIS_DISTRIBUTED_LOCK_HOLDERS.get(fileName);
		if (jedisDistributedLockHolder != null) {
			return jedisDistributedLockHolder;
		}
		synchronized (JEDIS_DISTRIBUTED_LOCK_HOLDERS) {
			jedisDistributedLockHolder = JEDIS_DISTRIBUTED_LOCK_HOLDERS.get(fileName);
			if (jedisDistributedLockHolder == null) {
				Pool<Jedis> pool = JedisPoolFactory.getJedisPool(fileName);
				jedisDistributedLockHolder = new JedisDistributedLockHolder(pool,
						jedisLockInjector.lockExpiryInMillis(), jedisLockInjector.intervalInMillis(),
						jedisLockInjector.useWatchDog());
				JEDIS_DISTRIBUTED_LOCK_HOLDERS.put(fileName, jedisDistributedLockHolder);
			}
			return jedisDistributedLockHolder;
		}
	}

	public static void initField(Object target, Class<?> serviceType) throws Throwable {
		Field[] fields = serviceType.getDeclaredFields();
		for (Field field : fields) {
			JedisLockInjector jedisLockInjector = field.getAnnotation(JedisLockInjector.class);
			if (!field.getType().isPrimitive()) {
				Class<?> serviceClass = field.getType();
				System.out.println("serviceClass:" + serviceClass);
				if (jedisLockInjector != null && JedisDistributedLockHolder.class.isAssignableFrom(serviceClass)) {
					field.setAccessible(true);
					System.out.println("holder :" + getJedisDistributedLockHolder(jedisLockInjector));
					field.set(target, getJedisDistributedLockHolder(jedisLockInjector));
				}
			}
		}
	}

}