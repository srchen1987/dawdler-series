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
package club.dawdler.clientplug.web.session;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.session.base.SessionIdGeneratorBase;
import club.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import club.dawdler.clientplug.web.session.message.MessageOperator;
import club.dawdler.clientplug.web.session.store.SessionStore;
import club.dawdler.core.serializer.Serializer;

import jakarta.servlet.ServletContext;

/**
 * @author jackson.song
 * @version V1.0
 * session操作者
 */
public class SessionOperator {
	private static final Logger logger = LoggerFactory.getLogger(SessionOperator.class);
	private final AbstractDistributedSessionManager abstractDistributedSessionManager;
	private final ServletContext servletContext;
	private static final Map<String, Object> SESSIONKEY_LOCK = new ConcurrentHashMap<>();
	private final SessionStore sessionStore;
	private final Serializer serializer;
	private final MessageOperator messageOperator;
	private final SessionIdGeneratorBase sessionIdGenerator;

	public SessionOperator(AbstractDistributedSessionManager abstractDistributedSessionManager,
			SessionIdGeneratorBase sessionIdGenerator, SessionStore sessionStore, MessageOperator messageOperator,
			Serializer serializer, ServletContext servletContext) {
		this.abstractDistributedSessionManager = abstractDistributedSessionManager;
		this.sessionIdGenerator = sessionIdGenerator;
		this.sessionStore = sessionStore;
		this.serializer = serializer;
		this.servletContext = servletContext;
		this.messageOperator = messageOperator;

	}

	public static void reloadAttributes(Map<byte[], byte[]> data, DawdlerHttpSession session, Serializer serializer) {
		ConcurrentHashMap<String, Object> attribute = new ConcurrentHashMap<>();
		for (Entry<byte[], byte[]> entry : data.entrySet()) {
			String key = new String(entry.getKey());
			try {
				Object obj = serializer.deserialize(entry.getValue());
				if (key.equals(DawdlerHttpSession.CREATION_TIME_KEY)) {
					session.setCreationTime((Long) obj);
				} else if (key.equals(DawdlerHttpSession.LAST_ACCESSED_TIME_KEY)) {
					session.setLastAccessedTime((Long) obj);
				} else {
					attribute.put(key, obj);
				}
			} catch (Exception e) {
				logger.error("", e);
				session.getAttributesRemoveNewKeys().add(key);
			}
		}
		session.setNew(false);
		session.setAttributes(attribute);
	}

	public DawdlerHttpSession operationSession(String sessionKey, int maxInactiveInterval) throws Exception {
		DawdlerHttpSession session = abstractDistributedSessionManager.getSession(sessionKey);
		if (session == null) {
			Object sessionLock = SESSIONKEY_LOCK.computeIfAbsent(sessionKey, lock -> new Object());
			try {
				synchronized (sessionLock) {
					session = abstractDistributedSessionManager.getSession(sessionKey);
					if (session == null) {
						Map<byte[], byte[]> data = sessionStore.getAttributes(sessionKey);
						if (!data.isEmpty()) {
							session = createLocalSession(sessionKey, maxInactiveInterval, false);
							reloadAttributes(data, session, serializer);
							addSession(session);
						}
					}
				}
			} finally {
				SESSIONKEY_LOCK.remove(sessionKey);
			}
		}
		return session;
	}

	public DawdlerHttpSession createLocalSession(String sessionKey, int maxInactiveInterval, boolean newSession) {
		String sessionSign = sessionIdGenerator.generateSessionId();
		DawdlerHttpSession session = new DawdlerHttpSession(sessionKey, sessionSign, this, messageOperator,
				servletContext, newSession);
		session.setMaxInactiveInterval(maxInactiveInterval);
		return session;
	}

	public void addSession(DawdlerHttpSession session) {
		abstractDistributedSessionManager.addSession(session.getId(), session);
	}

	public void removeSession(String sessionKey) {
		abstractDistributedSessionManager.removeSession(sessionKey);
		try {
			sessionStore.removeSession(sessionKey);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
