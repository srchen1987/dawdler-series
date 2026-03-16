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
package club.dawdler.clientplug.web.session.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.session.AbstractDistributedSessionManager;
import club.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import club.dawdler.clientplug.web.session.message.RedisMessageOperator;
import club.dawdler.core.serializer.Serializer;
import redis.clients.jedis.AbstractPipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.UnifiedJedis;

/**
 * @author jackson.song
 * @version V1.0
 * session存储 基于redis的实现
 */
public class RedisSessionStore implements SessionStore {
	public static final String SESSION_KEY_PREFIX = "session:";
	public static final String IP_PREFIX = "ip:";
	private static final Logger logger = LoggerFactory.getLogger(RedisSessionStore.class);
	private final UnifiedJedis unifiedJedis;
	private final Serializer serializer;
	GetAttributesJedisExecutor getAttributesJedisExecutor = new GetAttributesJedisExecutor();
	GetAttributeJedisExecutor getAttributeJedisExecutor = new GetAttributeJedisExecutor();
	SaveSessionJedisExecutor saveSessionJedisExecutor = new SaveSessionJedisExecutor();
	RemoveSessionJedisExecutor removeSessionJedisExecutor = new RemoveSessionJedisExecutor();

	public RedisSessionStore(UnifiedJedis unifiedJedis, Serializer serializer) {
		this.unifiedJedis = unifiedJedis;
		this.serializer = serializer;
	}

	private <T> T execute(UnifiedJedis unifiedJedis, JedisExecutor<T> executor, Object attr) throws Exception {
		return executor.execute(unifiedJedis, attr);
	}

	/**
	 * @param session
	 * @return void
	 * @throws Exception
	 * 将session序列化到redis中，由于redis不支持put与expire一起执行，lua写又没办法传入hmap结构
	 * 所以采用了pipeline
	 * @author jackson.song
	 */
	@Override
	public void saveSession(DawdlerHttpSession session) throws Exception {
		execute(unifiedJedis, saveSessionJedisExecutor, session);
	}

	@Override
	public Map<byte[], byte[]> getAttributes(String sessionKey) throws Exception {
		return execute(unifiedJedis, getAttributesJedisExecutor, SESSION_KEY_PREFIX + sessionKey);
	}

	@Override
	public byte[] getAttribute(String sessionKey, String attribute) throws Exception {
		return execute(unifiedJedis, getAttributeJedisExecutor,
				new byte[][] { (SESSION_KEY_PREFIX + sessionKey).getBytes(), attribute.getBytes() });
	}

	@Override
	public void removeSession(String sessionKey) throws Exception {
		execute(unifiedJedis, removeSessionJedisExecutor, sessionKey);
	}

	public UnifiedJedis getUnifiedJedis() {
		return unifiedJedis;
	}

	public interface JedisExecutor<T> {
		T execute(UnifiedJedis unifiedJedis, Object attr) throws Exception;
	}

	public class GetAttributesJedisExecutor implements JedisExecutor<Map<byte[], byte[]>> {
		@Override
		public Map<byte[], byte[]> execute(UnifiedJedis unifiedJedis, Object attr) throws Exception {
			return unifiedJedis.hgetAll(attr.toString().getBytes());
		}
	}

	public class GetAttributeJedisExecutor implements JedisExecutor<byte[]> {
		@Override
		public byte[] execute(UnifiedJedis unifiedJedis, Object attr) throws Exception {
			byte[][] data = (byte[][]) attr;
			return unifiedJedis.hget(data[0], data[1]);
		}
	}

	public class SaveSessionJedisExecutor implements JedisExecutor<Void> {
		@Override
		public Void execute(UnifiedJedis unifiedJedis, Object attr) throws Exception {
			DawdlerHttpSession session = (DawdlerHttpSession) attr;
			String id = SESSION_KEY_PREFIX + session.getId();
			AbstractPipeline pipeline = unifiedJedis.pipelined();
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
		public Void execute(UnifiedJedis unifiedJedis, Object attr) throws Exception {
			unifiedJedis.del(SESSION_KEY_PREFIX + attr);
			return null;
		}
	}

	@Override
	public void saveSession(DawdlerHttpSession session, String ip, AbstractDistributedSessionManager sessionManager,
			boolean defense, int ipLimit, int ipMaxInactiveInterval) throws Exception {
			String id = SESSION_KEY_PREFIX + session.getId();
			AbstractPipeline pipeline = unifiedJedis.pipelined();
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

	}

}
