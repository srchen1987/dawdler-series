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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.session.AbstractDistributedSessionManager;
import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.anywide.dawdler.clientplug.web.session.message.RedisMessageOperator;
import com.anywide.dawdler.core.serializer.Serializer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * session存储 基于redis的实现
 */
public class RedisSessionStore implements SessionStore {
	public static final String SESSION_KEY_PREFIX = "session:";
	public static final String IP_PREFIX = "ip:";
	private static final Logger logger = LoggerFactory.getLogger(RedisSessionStore.class);
	private final Pool<Jedis> jedisPool;
	private final Serializer serializer;
	GetAttributesJedisExecutor getAttributesJedisExecutor = new GetAttributesJedisExecutor();
	GetAttributeJedisExecutor getAttributeJedisExecutor = new GetAttributeJedisExecutor();
	SaveSessionJedisExecutor saveSessionJedisExecutor = new SaveSessionJedisExecutor();
	RemoveSessionJedisExecutor removeSessionJedisExecutor = new RemoveSessionJedisExecutor();

	public RedisSessionStore(Pool<Jedis> jedisPool, Serializer serializer) {
		this.jedisPool = jedisPool;
		this.serializer = serializer;
	}

	private <T> T execute(Pool<Jedis> jedisPool, JedisExecutor<T> executor, Object attr) throws Exception {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return executor.execute(jedis, attr);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	/**
	 * @param session
	 * @return void
	 * @throws Exception
	 * 将session序列化到redis中，由于redis不支持put与expire一起执行，lua写又没办法传入hmap结构
	 *              所以采用了pipeline
	 * @author jackson.song
	 */
	@Override
	public void saveSession(DawdlerHttpSession session) throws Exception {
		execute(jedisPool, saveSessionJedisExecutor, session);
	}

	@Override
	public Map<byte[], byte[]> getAttributes(String sessionKey) throws Exception {
		return execute(jedisPool, getAttributesJedisExecutor, SESSION_KEY_PREFIX + sessionKey);
	}

	@Override
	public byte[] getAttribute(String sessionKey, String attribute) throws Exception {
		return execute(jedisPool, getAttributeJedisExecutor,
				new byte[][] { (SESSION_KEY_PREFIX + sessionKey).getBytes(), attribute.getBytes() });
	}

	@Override
	public void removeSession(String sessionKey) throws Exception {
		execute(jedisPool, removeSessionJedisExecutor, sessionKey);
	}

	public Pool<Jedis> getJedisPool() {
		return jedisPool;
	}

	public interface JedisExecutor<T> {
		T execute(Jedis jedis, Object attr) throws Exception;
	}

	public class GetAttributesJedisExecutor implements JedisExecutor<Map<byte[], byte[]>> {
		@Override
		public Map<byte[], byte[]> execute(Jedis jedis, Object attr) throws Exception {
			return jedis.hgetAll(attr.toString().getBytes());
		}
	}

	public class GetAttributeJedisExecutor implements JedisExecutor<byte[]> {
		@Override
		public byte[] execute(Jedis jedis, Object attr) throws Exception {
			byte[][] data = (byte[][]) attr;
			return jedis.hget(data[0], data[1]);
		}
	}

	public class SaveSessionJedisExecutor implements JedisExecutor<Void> {
		@Override
		public Void execute(Jedis jedis, Object attr) throws Exception {
			DawdlerHttpSession session = (DawdlerHttpSession) attr;
			String id = SESSION_KEY_PREFIX + session.getId();
			Pipeline pipeline = jedis.pipelined();
			Map<String, Object> attributesAddNew = session.getAttributesAddNew();
			boolean add = !attributesAddNew.isEmpty();
			if (add) {
				Map<byte[], byte[]> addData = new HashMap<>();
				attributesAddNew.forEach((k, v) -> {
					try {
						addData.put(k.getBytes(), serializer.serialize(v));
					} catch (Exception e) {
						logger.error("", e);
					}
				});
				pipeline.hmset(id.getBytes(), addData);
			}

			List<String> removeKeys = session.getAttributesRemoveNewKeys();
			boolean del = !removeKeys.isEmpty();
			if (del) {
				pipeline.hdel(id, removeKeys.toArray(new String[0]));
			}
			if (add || del) {
				pipeline.publish(RedisMessageOperator.CHANNEL_ATTRIBUTE_CHANGE_RELOAD,
						id + "$" + session.getSessionSign());
			}
			Response<Long> exist = pipeline.expire(id, session.getMaxInactiveInterval() - 5);
			pipeline.close();
			if (exist.get() == 0) {
				session.invalidate();
			}
			return null;
		}
	}

	public class RemoveSessionJedisExecutor implements JedisExecutor<Void> {
		@Override
		public Void execute(Jedis jedis, Object attr) throws Exception {
			jedis.del(SESSION_KEY_PREFIX + attr);
			return null;
		}
	}

	@Override
	public void saveSession(DawdlerHttpSession session, String ip, AbstractDistributedSessionManager sessionManager,
			boolean defense, int ipLimit, int ipMaxInactiveInterval) throws Exception {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String id = SESSION_KEY_PREFIX + session.getId();
			Pipeline pipeline = jedis.pipelined();
			Map<String, Object> attributesAddNew = session.getAttributesAddNew();
			boolean add = !attributesAddNew.isEmpty();
			if (add) {
				Map<byte[], byte[]> addData = new HashMap<>();
				attributesAddNew.forEach((k, v) -> {
					try {
						addData.put(k.getBytes(), serializer.serialize(v));
					} catch (Exception e) {
						logger.error("", e);
					}
				});
				pipeline.hmset(id.getBytes(), addData);
			}

			List<String> removeKeys = session.getAttributesRemoveNewKeys();
			boolean del = !removeKeys.isEmpty();
			if (del) {
				pipeline.hdel(id, removeKeys.toArray(new String[0]));
			}
			if (add || del) {
				pipeline.publish(RedisMessageOperator.CHANNEL_ATTRIBUTE_CHANGE_RELOAD,
						id + "$" + session.getSessionSign());
			}
			String ipKey = IP_PREFIX + ip;
			Response<Long> ipCount = pipeline.incr(ipKey);
			pipeline.expire(ipKey, ipMaxInactiveInterval);
			Response<Long> exist = pipeline.expire(id, session.getMaxInactiveInterval() - 5);
			if (pipeline != null) {
				pipeline.close();
			}
			if (exist.get() == 0) {
				session.invalidate();
			}
			if (ipCount.get() > ipLimit) {
				sessionManager.addIpToBlacklist(ip);
			}
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}

	}

}
