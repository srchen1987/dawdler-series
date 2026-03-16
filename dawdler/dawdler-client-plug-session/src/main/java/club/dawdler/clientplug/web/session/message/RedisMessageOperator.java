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
package club.dawdler.clientplug.web.session.message;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.session.AbstractDistributedSessionManager;
import club.dawdler.clientplug.web.session.SessionOperator;
import club.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import club.dawdler.clientplug.web.session.store.RedisSessionStore;
import club.dawdler.clientplug.web.session.store.SessionStore;
import club.dawdler.core.serializer.Serializer;
import club.dawdler.jedis.UnifiedJedisWarpper;
import jakarta.servlet.http.HttpSessionEvent;
import redis.clients.jedis.AbstractPipeline;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol.Command;
import redis.clients.jedis.Protocol.Keyword;
import redis.clients.jedis.UnifiedJedis;

/**
 * @author jackson.song
 * @version V1.0
 * redis实现的消息操作者
 */
public class RedisMessageOperator implements MessageOperator {
	public static final String CHANNEL_ATTRIBUTE_CHANGE_RELOAD = "__keyevent__:attribute_change_reload";
	public static final String CHANNEL_ATTRIBUTE_CHANGE = "__keyevent__:attribute_change";
	public static final String CHANNEL_ATTRIBUTE_CHANGE_DEL = "__keyevent__:attribute_del";
	private static final Logger logger = LoggerFactory.getLogger(RedisMessageOperator.class);
	private final Serializer serializer;
	private final SessionStore sessionStore;
	private final AbstractDistributedSessionManager abstractDistributedSessionManager;
	private String channelExpired = "__keyevent@database__:expired";
	private String channelDel = "__keyevent@database__:del";
	private UnifiedJedisWarpper unifiedJedisWarpper;
	private UnifiedJedis unifiedJedis;
	private volatile boolean start = true;

	public RedisMessageOperator(Serializer serializer, SessionStore sessionStore,
			AbstractDistributedSessionManager abstractDistributedSessionManager,
			UnifiedJedisWarpper unifiedJedisWarpper) {
		this.serializer = serializer;
		this.sessionStore = sessionStore;
		this.abstractDistributedSessionManager = abstractDistributedSessionManager;
		this.unifiedJedisWarpper = unifiedJedisWarpper;
		this.unifiedJedis = unifiedJedisWarpper.getUnifiedJedis();
	}

	private static void config(UnifiedJedis unifiedJedis) {
		String parameter = "notify-keyspace-events";
		CommandArguments commandArguments = new CommandArguments((Command.CONFIG));
		CommandObject<Map<String, String>> commandObject = new CommandObject<>(
				commandArguments.add(Keyword.GET).add(parameter), BuilderFactory.STRING_MAP);
		Map<String, String> notify = unifiedJedis.executeCommand(commandObject);
		if (notify.containsKey(parameter) && notify.get(parameter).equals("")) {
			unifiedJedis.configSet(parameter, "gxE");
		}
	}

	Thread thread = null;

	@Override
	public void listenExpireAndDelAndChange() {
		channelExpired = channelExpired.replace("database", unifiedJedisWarpper.getDatabase() +
				"");
		channelDel = channelDel.replace("database", unifiedJedisWarpper.getDatabase() + "");
		thread = new Thread(() -> {
			while (start) {
				try {
					config(unifiedJedis);
					subscribe(unifiedJedis);
				} catch (Exception e) {
					abstractDistributedSessionManager.invalidateAll();
					if (start) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
						}
					}
				}
			}
		}, "listenExpireAndDelAndChangeThread");
		thread.setDaemon(true);
		thread.start();
	}

	private void subscribe(UnifiedJedis unifiedJedis) {
				unifiedJedis.subscribe(new ResponseDataListener(), channelExpired, channelDel, CHANNEL_ATTRIBUTE_CHANGE,
				CHANNEL_ATTRIBUTE_CHANGE_RELOAD, CHANNEL_ATTRIBUTE_CHANGE_DEL);
	}

	@Override
	public void sendMessageToDel(String sessionKey, String attributeName) {
		try {
			AbstractPipeline pipeline = unifiedJedis.pipelined();
			pipeline.hdel(sessionKey, attributeName);
			pipeline.publish(CHANNEL_ATTRIBUTE_CHANGE_DEL, sessionKey + "$" + attributeName);
			pipeline.close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public void sendMessageToSet(String sessionKey, String attributeName, Object attributeValue) {
		try {
			AbstractPipeline pipeline = unifiedJedis.pipelined();
			pipeline.hset(sessionKey.getBytes(), attributeName.getBytes(), serializer.serialize(attributeValue));
			pipeline.publish(CHANNEL_ATTRIBUTE_CHANGE, sessionKey + "$" + attributeName);
			pipeline.close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public class ResponseDataListener extends JedisPubSub {
		private String[] splitMessage(String message) {
			return message.split("\\$");
		}

		@Override
		public void onMessage(String channel, String message) {
			if (!message.startsWith(RedisSessionStore.SESSION_KEY_PREFIX)) {
				return;
			}
			boolean ischange = CHANNEL_ATTRIBUTE_CHANGE.equals(channel);
			if (ischange || CHANNEL_ATTRIBUTE_CHANGE_DEL.equals(channel)) {
				String[] data = splitMessage(message);
				String sessionKey = data[0].replace(RedisSessionStore.SESSION_KEY_PREFIX, "");
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
				String sessionKey = data[0].replace(RedisSessionStore.SESSION_KEY_PREFIX, "");
				DawdlerHttpSession session = abstractDistributedSessionManager.getSession(sessionKey);
				if (session != null) {
					if (CHANNEL_ATTRIBUTE_CHANGE_RELOAD.equals(channel)) {
						if (session.getSessionSign().equals(data[1])) {
							return;
						}
						try {
							SessionOperator.reloadAttributes(sessionStore.getAttributes(session.getId()), session,
									serializer);
						} catch (Exception e) {
							logger.error("", e);
						}
					} else {
						jakarta.servlet.http.HttpSessionListener httpSessionListener = abstractDistributedSessionManager
								.getHttpSessionListener();
						if (httpSessionListener != null) {
							if (channelExpired.equals(channel)) {
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
		if (unifiedJedis != null) {
			try {
				unifiedJedis.close();
			} catch (Exception e) {
			}
		}
	}
}
