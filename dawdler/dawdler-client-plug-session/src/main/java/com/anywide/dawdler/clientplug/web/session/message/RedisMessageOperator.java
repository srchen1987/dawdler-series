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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.session.AbstractDistributedSessionManager;
import com.anywide.dawdler.clientplug.web.session.SessionOperator;
import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.anywide.dawdler.clientplug.web.session.store.RedisSessionStore;
import com.anywide.dawdler.clientplug.web.session.store.SessionStore;
import com.anywide.dawdler.core.serializer.Serializer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolAbstract;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
/**
 * 
 * @Title:  MessageOperator.java
 * @Description:  redis实现的消息操作者
 * @author: jackson.song    
 * @date:   2016年6月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class RedisMessageOperator implements MessageOperator {
	private Serializer serializer;
	private SessionStore sessionStore;
	private AbstractDistributedSessionManager abstractDistributedSessionManager;
	public static final String CHANNEL_EXPIRED = "__keyevent@0__:expired";
	public static final String CHANNEL_DEL = "__keyevent@0__:del";
	public static final String CHANNEL_ATTRIBUTECHANGE_RELOAD = "__keyevent__:attribute_change_reload";
	public static final String CHANNEL_ATTRIBUTECHANGE = "__keyevent__:attribute_change";
	public static final String CHANNEL_ATTRIBUTECHANGE_DEL = "__keyevent__:attribute_del";
	public JedisPoolAbstract jedisPoolAbstract;
	public static Logger logger = LoggerFactory.getLogger(RedisMessageOperator.class);

	public RedisMessageOperator(Serializer serializer, SessionStore sessionStore,
			AbstractDistributedSessionManager abstractDistributedSessionManager, JedisPoolAbstract jedisPoolAbstract) {
		this.serializer = serializer;
		this.sessionStore = sessionStore;
		this.abstractDistributedSessionManager = abstractDistributedSessionManager;
		this.jedisPoolAbstract = jedisPoolAbstract;
	}


	@Override
	public void listenExpireAndDelAndChange() {
		new Thread(() -> {
				Jedis jedis = null;
				try {
					jedis = jedisPoolAbstract.getResource();
					config(jedis);
//					jedis.subscribe(new ResponseDataListener(),"__keyevent@0__:expired", "__keyevent@0__:del","__keyevent@0__:hset");
					jedis.subscribe(new ResponseDataListener(), CHANNEL_EXPIRED, CHANNEL_DEL, CHANNEL_ATTRIBUTECHANGE,
							CHANNEL_ATTRIBUTECHANGE_RELOAD, CHANNEL_ATTRIBUTECHANGE_DEL);
				} catch (Exception e) {
					logger.error("",e);
					if (jedis != null)
						try {
							jedis.close();
						} catch (Exception e1) {
						}
				}
		}).start();

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
	

	public class ResponseDataListener extends JedisPubSub {
		private String[] splitMessage(String message) {
			int index = message.indexOf("$");
			String sessionkey = message.substring(0, index);
			String attribute = message.substring(index+1);
			return new String[] { sessionkey, attribute };
		}

		@Override
		public void onMessage(String channel, String message) {
			boolean ischange = CHANNEL_ATTRIBUTECHANGE.equals(channel);
			if (ischange || CHANNEL_ATTRIBUTECHANGE_DEL.equals(channel)) {
				String[] data = splitMessage(message);
				String sessionkey = data[0].replace(RedisSessionStore.SESSIONKEY_PREFIX, "");
				String attribute = data[1];
				DawdlerHttpSession session = abstractDistributedSessionManager.getSession(sessionkey);
				if (session != null) {
					if (ischange) {
						try {
							byte[] valueByte = sessionStore.getAttribute(sessionkey, attribute);
							if (valueByte != null) {
								session.setAttributeFromNotify(sessionkey, serializer.deserialize(valueByte));
							}
						} catch (Exception e) {
							logger.error("", e);
						}
					} else {
						session.removeAttributeFromNotify(attribute);
					}
				}
			} else {
				String sessionkey = message.replace(RedisSessionStore.SESSIONKEY_PREFIX, "");
				DawdlerHttpSession session = abstractDistributedSessionManager.getSession(sessionkey);
				if (session != null) {
					if (CHANNEL_ATTRIBUTECHANGE_RELOAD.equals(channel)) {
						session.clear(); 
						try {
							SessionOperator.reloadAttributes(sessionStore.getAttributes(session.getId()), session,
									serializer);
						} catch (Exception e) {
							logger.error("", e);
						}
					} else {
						abstractDistributedSessionManager.removeSession(session);
					}
				}
			}
		}

//	    	// 初始化订阅时候的处理
//	    	public void onSubscribe(String channel, int subscribedChannels) {
//	    		 System.out.println("onSubscribe:"+channel + "=" + subscribedChannels);
//	    	}
//
//	    	// 取消订阅时候的处理
//	    	public void onUnsubscribe(String channel, int subscribedChannels) {
//	    		 System.out.println("onUnsubscribe:"+channel + "=" + subscribedChannels);
//	    	}
//
//	    	// 初始化按表达式的方式订阅时候的处理
//	    	public void onPSubscribe(String pattern, int subscribedChannels) {
//	    		 System.out.println("onPSubscribe:"+pattern + "=" + subscribedChannels);
//	    	}
//
//	    	// 取消按表达式的方式订阅时候的处理
//	    	public void onPUnsubscribe(String pattern, int subscribedChannels) {
//	    		 System.out.println("onPUnsubscribe:"+pattern + "=" + subscribedChannels);
//	    	}
//
//	    	// 取得按表达式的方式订阅的消息后的处理
//	    	public void onPMessage(String pattern, String channel, String message) {
//	    		System.out.println("onPMessage:"+pattern + "=" + channel + "=" + message);
//	    	}

	}

	@Override
	public void sendMessageToDel(String sessionkey, String attributeName) {
		Jedis jedis = jedisPoolAbstract.getResource();
		try {
			Pipeline pipeline = jedis.pipelined();
			pipeline.hdel(sessionkey, attributeName);
			pipeline.publish(CHANNEL_ATTRIBUTECHANGE_DEL, sessionkey+"$"+attributeName);
			pipeline.sync();
		} catch (Exception e) {
			logger.error("", e);
		}finally {
			if(jedis != null)
				jedis.close();
		}
	}

	@Override
	public void sendMessageToSet(String sessionkey, String attributeName, Object attributeValue) {
		Jedis jedis = jedisPoolAbstract.getResource();
		try {
			Pipeline pipeline = jedis.pipelined();
			pipeline.hset(sessionkey.getBytes(), attributeName.getBytes(), serializer.serialize(attributeValue));
			pipeline.publish(CHANNEL_ATTRIBUTECHANGE, sessionkey+"$"+attributeName);
			pipeline.sync();
		} catch (Exception e) {
			logger.error("", e);
		}finally {
			if(jedis != null)
				jedis.close();
		}
	}
}
