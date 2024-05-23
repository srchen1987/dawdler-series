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
package com.anywide.dawdler.cache.jedis;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.anywide.dawdler.cache.CacheConfig;
import com.anywide.dawdler.cache.exception.KeyExpressionException;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.serializer.Serializer;
import com.anywide.dawdler.jedis.JedisOperator;
import com.anywide.dawdler.jedis.JedisOperatorFactory;
import com.anywide.dawdler.util.ClassUtil;

/**
 * @author jackson.song
 * @version V1.0
 * JedisInnerCache Jedis实现的内部操作Cache类
 */
public class JedisInnerCache {
	private JedisOperator jedisOperator;
	private Serializer serializer;
	private String keyPrefix;
	private Long expireAfterAccessSeconds;
	private Long expireAfterWriteSeconds;

	public JedisInnerCache(CacheConfig cacheConfig) throws Exception {
		this.keyPrefix = cacheConfig.getName() + ":";
		String fileName = cacheConfig.getFileName();
		this.serializer = SerializeDecider.decide(cacheConfig.getSerializeType().getType());
		this.jedisOperator = JedisOperatorFactory.getJedisOperator(fileName);
		Duration expireAfterAccessDuration = cacheConfig.getExpireAfterAccessDuration();
		if (expireAfterAccessDuration != null) {
			expireAfterAccessSeconds = expireAfterAccessDuration.getSeconds();
		}
		Duration expireAfterWriteDuration = cacheConfig.getExpireAfterWriteDuration();
		if (expireAfterWriteDuration != null) {
			expireAfterWriteSeconds = expireAfterWriteDuration.getSeconds();
		} else {
			expireAfterWriteSeconds = expireAfterAccessSeconds;
		}
	}

	public Object get(Object key) throws Exception {
		byte[] byteKey = convertKey(key).getBytes();
		byte[] data = jedisOperator.get(byteKey);
		if (data != null) {
			Object obj = serializer.deserialize(data);
			if (expireAfterAccessSeconds != null) {
				jedisOperator.expire(byteKey, expireAfterAccessSeconds);
			}
			return obj;
		}
		return null;
	}

	public void del(Object key) {
		jedisOperator.del(convertKey(key).getBytes());
	}

	public void delAll() {
		Optional.ofNullable(jedisOperator.keys((keyPrefix + "*").getBytes())).ifPresent(new Consumer<Set<byte[]>>() {
			@Override
			public void accept(Set<byte[]> keys) {
				if (!keys.isEmpty()) {
					jedisOperator.del(keys.toArray(new byte[0][]));
				}
			}
		});

	}

	public void put(Object key, Object value) throws Exception {
		if (expireAfterWriteSeconds != null) {
			jedisOperator.setex((convertKey(key).getBytes()), expireAfterWriteSeconds, serializer.serialize(value));
		} else {
			jedisOperator.set(convertKey(key).getBytes(), serializer.serialize(value));
		}
	}

	public String convertKey(Object key) {
		Class<?> type = key.getClass();
		if (type != String.class && !ClassUtil.isPrimitiveOrWrapper(key.getClass())) {
			throw new KeyExpressionException("key type must be String or primitive or primitive wrapper !");
		}
		return keyPrefix + key.toString();
	}
	
}
