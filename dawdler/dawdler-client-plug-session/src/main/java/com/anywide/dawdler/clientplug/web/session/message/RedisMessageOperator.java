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
package com.anywide.dawdler.clientplug.web.session.message;

import java.util.List;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.session.AbstractDistributedSessionManager;
import com.anywide.dawdler.clientplug.web.session.SessionOperator;
import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.anywide.dawdler.clientplug.web.session.store.RedisSessionStore;
import com.anywide.dawdler.clientplug.web.session.store.SessionStore;
import com.anywide.dawdler.core.serializer.Serializer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title MessageOperator.java
 * @Description redis实现的消息操作者
 * @date 2016年6月16日
 * @email suxuan696@gmail.com
 */
public class RedisMessageOperator implements MessageOperator {
	public static final String CHANNEL_ATTRIBUTE_CHANGE_RELOAD = "__keyevent__:attribute_change_reload";
	public static final String CHANNEL_ATTRIBUTE_CHANGE = "__keyevent__:attribute_change";
	public static final String CHANNEL_ATTRIBUTE_CHANGE_DEL = "__keyevent__:attribute_del";
	public static Logger logger = LoggerFactory.getLogger(RedisMessageOperator.class);
	private final Serializer serializer;
	private final SessionStore sessionStore;
	private final AbstractDistributedSessionManager abstractDistributedSessionManager;
	public String CHANNEL_EXPIRED = "__keyevent@database__:expired";
	public String CHANNEL_DEL = "__keyevent@database__:del";
	public Pool<Jedis> jedisPool;
	private volatile boolean start = true;
	private Jedis jedis = null;

	public RedisMessageOperator(Serializer serializer, SessionStore sessionStore,
			AbstractDistributedSessionManager abstractDistributedSessionManager, Pool<Jedis> jedisPool) {
		this.serializer = serializer;
		this.sessionStore = sessionStore;
		this.abstractDistributedSessionManager = abstractDistributedSessionManager;
		this.jedisPool = jedisPool;
	}

	// config set notify-keyspace-events Ex
	// config set notify-keyspace-events AKE
	// config set notify-keyspace-events gxE
	private static void config(Jedis jedis) {
		String parameter = "notify-keyspace-events";
		List<String> notify = jedis.configGet(parameter);
		if (notify.get(1).equals("")) {
//          jedis.configSet(parameter, "Ex"); //过期事件
//        	jedis.configSet(parameter, "AKE"); //全部事件 包括 hset 等
			jedis.configSet(parameter, "gxE");// 过期与删除
		}
	}

	Thread thread = null;

	@Override
	public void listenExpireAndDelAndChange() {
		thread = new Thread(() -> {
			while (start) {
				try {
					jedis = jedisPool.getResource();
					config(jedis);
					CHANNEL_EXPIRED = CHANNEL_EXPIRED.replace("database", jedis.getDB() + "");
					CHANNEL_DEL = CHANNEL_DEL.replace("database", jedis.getDB() + "");
					subscribe(jedis);
				} catch (Exception e) {
					abstractDistributedSessionManager.invalidateAll();
					if (jedis != null) {
						try {
							jedis.close();
						} catch (Exception e1) {
						}
					}
					if(start) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
						}
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void subscribe(Jedis jedis) {
		jedis.subscribe(new ResponseDataListener(), CHANNEL_EXPIRED, CHANNEL_DEL, CHANNEL_ATTRIBUTE_CHANGE,
				CHANNEL_ATTRIBUTE_CHANGE_RELOAD, CHANNEL_ATTRIBUTE_CHANGE_DEL);
	}

	@Override
	public void sendMessageToDel(String sessionKey, String attributeName) {
		Jedis jedis = jedisPool.getResource();
		try {
			Pipeline pipeline = jedis.pipelined();
			pipeline.hdel(sessionKey, attributeName);
			pipeline.publish(CHANNEL_ATTRIBUTE_CHANGE_DEL, sessionKey + "$" + attributeName);
			pipeline.close();
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	@Override
	public void sendMessageToSet(String sessionKey, String attributeName, Object attributeValue) {
		Jedis jedis = jedisPool.getResource();
		try {
			Pipeline pipeline = jedis.pipelined();
			pipeline.hset(sessionKey.getBytes(), attributeName.getBytes(), serializer.serialize(attributeValue));
			pipeline.publish(CHANNEL_ATTRIBUTE_CHANGE, sessionKey + "$" + attributeName);
			pipeline.close();
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	public class ResponseDataListener extends JedisPubSub {
		private String[] splitMessage(String message) {
			return message.split("\\$");
		}

		@Override
		public void onMessage(String channel, String message) {
			if (!message.startsWith(RedisSessionStore.SESSIONKEY_PREFIX)) {
				return;
			}
			boolean ischange = CHANNEL_ATTRIBUTE_CHANGE.equals(channel);
			if (ischange || CHANNEL_ATTRIBUTE_CHANGE_DEL.equals(channel)) {
				String[] data = splitMessage(message);
				String sessionKey = data[0].replace(RedisSessionStore.SESSIONKEY_PREFIX, "");
				String attribute = data[1];
				DawdlerHttpSession session = abstractDistributedSessionManager.getSession(sessionKey);
				if (session != null) {
					if (ischange) {
						try {
							byte[] valueByte = sessionStore.getAttribute(sessionKey, attribute);
							if (valueByte != null) {
								session.setAttributeFromNotify(sessionKey, serializer.deserialize(valueByte));
							}
						} catch (Exception e) {
							logger.error("", e);
						}
					} else {
						session.removeAttributeFromNotify(attribute);
					}
				}
			} else {
				String[] data = splitMessage(message);
				String sessionKey = data[0].replace(RedisSessionStore.SESSIONKEY_PREFIX, "");
				DawdlerHttpSession session = abstractDistributedSessionManager.getSession(sessionKey);
				if (session != null) {
					if (CHANNEL_ATTRIBUTE_CHANGE_RELOAD.equals(channel)) {
						if (session.getSessionSign().equals(data[1])) {
							return;
						}
						session.clear();
						try {
							SessionOperator.reloadAttributes(sessionStore.getAttributes(session.getId()), session,
									serializer);
						} catch (Exception e) {
							logger.error("", e);
						}
					} else {
						HttpSessionListener httpSessionListener = abstractDistributedSessionManager
								.getHttpSessionListener();
						if (httpSessionListener != null) {
							if (CHANNEL_EXPIRED.equals(channel)) {
								session.setExpiredEvent(true);
							}
							HttpSessionEvent httpSessionEvent = new HttpSessionEvent(session);
							httpSessionListener.sessionDestroyed(httpSessionEvent);
							session.innerInvalidate();
						}
						abstractDistributedSessionManager.removeSession(session);
					}
				}
			}
		}

	}

	@Override
	public void stop() {
		start = false;
		if (jedis != null) {
			try {
				jedis.close();
			} catch (Exception e) {
			}
		}
	}
}
