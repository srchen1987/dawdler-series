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
package com.anywide.dawdler.clientplug.web.session.http;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.anywide.dawdler.clientplug.web.session.SessionOperator;
import com.anywide.dawdler.clientplug.web.session.message.MessageOperator;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerHttpSession.java
 * @Description DawdlerSession的实现
 * @date 2016年6月16日
 * @email suxuan696@gmail.com
 */
public class DawdlerHttpSession implements HttpSession {
	public final static String CREATION_TIME_KEY = "creationTime";
	public final static String LAST_ACCESSED_TIME_KEY = "lastAccessedTime";
	public static ThreadLocal<Boolean> flushImmediately = new ThreadLocal<>();
	private final Map<String, Object> attributesAddNew = new HashMap<>();
	private final SessionOperator sessionOperator;
	private final MessageOperator messageOperator;
	private final List<String> attributesRemoveNewKeys = new CopyOnWriteArrayList<>();
	private final String sessionKey;
	private final ServletContext servletContext;
	protected volatile boolean isNew;
	private long creationTime;
	private long lastAccessedTime;
	private long lastFlushTime;
	private int maxInactiveInterval = 1800;
	private ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<>();
	private volatile boolean isValid = false;
	private boolean expiredEvent;// 是否是过期事件，还有一种情况是主动销毁,调用 invalidate，或直接触发通知(如删除sessionkey 在redis中)
	private String sessionSign;// 区分不同客户端的标识

	public DawdlerHttpSession(String sessionKey, String sessionSign, SessionOperator sessionOperator,
			MessageOperator messageOperator, ServletContext servletContext, boolean newSession) {
		this.servletContext = servletContext;
		this.sessionOperator = sessionOperator;
		this.messageOperator = messageOperator;
		this.sessionKey = sessionKey;
		this.sessionSign = sessionSign;
		if (newSession) {
			creationTime = JVMTimeProvider.currentTimeMillis();
			lastAccessedTime = creationTime;
			attributesAddNew.put(CREATION_TIME_KEY, creationTime);
			attributesAddNew.put(LAST_ACCESSED_TIME_KEY, lastAccessedTime);
			isNew = true;
		}

	}

	public static boolean isFlushImmediately() {
		return flushImmediately.get() != null;
	}

	public static void setFlushImmediately() {
		flushImmediately.set(true);
	}

	public static void clearFlushImmediately() {
		flushImmediately.remove();
	}

	public boolean isValid() {
		return isValid;
	}

	public Map<String, Object> getAttributesAddNew() {
		return attributesAddNew;
	}

	public List<String> getAttributesRemoveNewKeys() {
		return attributesRemoveNewKeys;
	}

	public void setAttributes(ConcurrentHashMap<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public String getId() {
		return sessionKey;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	public long getLastFlushTime() {
		return lastFlushTime;
	}

	public void setLastFlushTime(long lastFlushTime) {
		this.lastFlushTime = lastFlushTime;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return attributes.keys();
	}

	@Override
	public String[] getValueNames() {
		return attributes.keySet().toArray(new String[0]);
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (value == null) {
			removeAttribute(name);
			return;
		}
		Object preObj = attributes.get(name);
		if (!(preObj != null && (preObj == value || preObj.equals(value)))) {
			attributesRemoveNewKeys.remove(name);
			attributes.put(name, value);
			if (isFlushImmediately()) {
				messageOperator.sendMessageToSet(sessionKey, name, value);
			} else {
				attributesAddNew.put(name, value);
			}
		}
	}

	public void setAttributeFromNotify(String name, Object value) {
		Object preObj = attributes.get(name);
		if (!(preObj != null && (preObj == value || preObj.equals(value)))) {
			attributesRemoveNewKeys.remove(name);
			attributes.put(name, value);
		}
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
		attributesAddNew.remove(name);
		if (isFlushImmediately()) {
			messageOperator.sendMessageToDel(sessionKey, name);
		} else {
			attributesRemoveNewKeys.add(name);
		}

	}

	public void removeAttributeFromNotify(String name) {
		attributes.remove(name);
		attributesAddNew.remove(name);
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}

	@Override
	public void invalidate() {
		this.isValid = true;
		clear();
		sessionOperator.removeSession(sessionKey);
	}

	public void innerInvalidate() {
		this.isValid = true;
		clear();
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public void finish() {
		attributesAddNew.clear();
		attributesRemoveNewKeys.clear();
		clearFlushImmediately();
		lastAccessedTime = JVMTimeProvider.currentTimeMillis();
	}

	public void clear() {
		attributesAddNew.clear();
		attributesRemoveNewKeys.clear();
		attributes.clear();
	}

	public boolean isExpiredEvent() {
		return expiredEvent;
	}

	public void setExpiredEvent(boolean expiredEvent) {
		this.expiredEvent = expiredEvent;
	}

	public String getSessionSign() {
		return sessionSign;
	}

	public void setSessionSign(String sessionSign) {
		this.sessionSign = sessionSign;
	}

}
