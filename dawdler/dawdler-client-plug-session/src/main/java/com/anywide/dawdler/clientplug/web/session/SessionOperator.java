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
package com.anywide.dawdler.clientplug.web.session;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.anywide.dawdler.clientplug.web.session.message.MessageOperator;
import com.anywide.dawdler.clientplug.web.session.store.SessionStore;
import com.anywide.dawdler.core.serializer.Serializer;
/**
 * 
 * @Title:  SessionOperator.java
 * @Description:    session操作者
 * @author: jackson.song    
 * @date:   2016年6月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class SessionOperator {
	private static Logger logger = LoggerFactory.getLogger(SessionOperator.class);
	private AbstractDistributedSessionManager abstractDistributedSessionManager;
	private ServletContext servletContext;
	private final Map<String, Object> sessionKey_lock = new ConcurrentHashMap<>();
	private SessionStore sessionStore;
	private Serializer serializer;
	private MessageOperator messageOperator;
	public SessionOperator(AbstractDistributedSessionManager abstractDistributedSessionManager, SessionStore sessionStore, MessageOperator messageOperator,Serializer serializer, ServletContext servletContext) {
		this.abstractDistributedSessionManager = abstractDistributedSessionManager;
		this.sessionStore = sessionStore;
		this.serializer = serializer;
		this.servletContext = servletContext;
		this.messageOperator = messageOperator;
	}
	public DawdlerHttpSession operationSession(String sessionkey,int maxInactiveInterval) throws Exception  {
		DawdlerHttpSession session = abstractDistributedSessionManager.getSession(sessionkey);
		if (session == null) {
			Object sessionLock = sessionKey_lock.computeIfAbsent(sessionkey, lock -> new Object());
			try {
				synchronized (sessionLock) {
					session = abstractDistributedSessionManager.getSession(sessionkey);
					if (session == null) {
						Map<byte[], byte[]> data = sessionStore.getAttributes(sessionkey);
						if (data != null) {  
							session = createLocalSession(sessionkey, maxInactiveInterval, false);
							reloadAttributes(data, session, serializer);
						}
					}
				}
			} finally {
				sessionKey_lock.remove(sessionkey);
			}
		}
		return session;
	}
	
	
	public static void reloadAttributes(Map<byte[], byte[]> data, DawdlerHttpSession session, Serializer serializer) {
			ConcurrentHashMap<String, Object> attribute = new ConcurrentHashMap<String, Object>();
			for(Entry<byte[],byte[]> entry : data.entrySet()) {
				String key = new String(entry.getKey());
				try {
					Object obj = serializer.deserialize(entry.getValue());
					if (key.equals(DawdlerHttpSession.CREATIONTIMEKEY)) {
						session.setCreationTime((Long) obj);
					} else if (key.equals(DawdlerHttpSession.LASTACCESSEDTIMEKEY)) {
						session.setLastAccessedTime((Long) obj);
					} else
						attribute.put(key, obj);
				} catch (Exception e) {
					logger.error("", e);
					session.getAttributesRemoveNewKeys().add(key);
				}
			}
			session.setNew(false);
			session.setAttributes(attribute);
	}
	
	public DawdlerHttpSession createLocalSession(String sessionkey, int maxInactiveInterval, boolean newSession){
		DawdlerHttpSession session = new DawdlerHttpSession(sessionkey, this, messageOperator,servletContext, newSession);
		abstractDistributedSessionManager.addSession(sessionkey, session);
		return session;
	}
	
	
	
	public void getAttribute(String sessionkey,String attribute){
		try {
			sessionStore.getAttribute(sessionkey, attribute);
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public void removeSession(String sessionkey){
		abstractDistributedSessionManager.removeSession(sessionkey);
		try {
			sessionStore.removeSession(sessionkey);
		} catch (Exception e) {
			logger.error("",e);
		}
	}
}
