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
/**
 * 
 * @Title:  DawdlerHttpSession.java
 * @Description:  DawdlerSession的实现
 * @author: jackson.song    
 * @date:   2016年6月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class DawdlerHttpSession implements HttpSession {
	private long creationTime;
	private long lastAccessedTime;
	public final static String CREATIONTIMEKEY = "creationTime";
	public final static String LASTACCESSEDTIMEKEY = "lastAccessedTime";
	/**
	 * Flag indicating whether this session is new or not.
	 */
	protected volatile boolean isNew;

	/**
	 * Flag indicating whether this session is valid or not.
	 */
	private volatile boolean isValid = false;

	public boolean isValid() {
		return isValid;
	}

	private ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<>();
	
	private Map<String, Object> attributesAddNew = new HashMap<>();
	
	public Map<String, Object> getAttributesAddNew() {
		return attributesAddNew;
	}

	public List<String> getAttributesRemoveNewKeys() {
		return attributesRemoveNewKeys;
	}

	private List<String> attributesRemoveNewKeys = new CopyOnWriteArrayList<>();
	
	private String sessionKey;
	
	 

	private ServletContext servletContext;

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	public DawdlerHttpSession(String sessionKey,ServletContext servletContext) {
		this.servletContext = servletContext;
		creationTime = System.currentTimeMillis();
		lastAccessedTime = System.currentTimeMillis();
		isNew = true;
		this.sessionKey = sessionKey;
	}

	public void setAttributes(ConcurrentHashMap<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public String getId() {
		return sessionKey;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {

	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
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
		if(value==null) {
			removeAttribute(name);
			return;
		}
		Object preObj = attributes.get(name);
		if(!(preObj != null&&(preObj == value || preObj.equals(value)))) {
			attributes.put(name, value);
			attributesAddNew.put(name, value);
			attributesRemoveNewKeys.remove(name);
		} 
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name,value);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
		attributesAddNew.remove(name);
		attributesRemoveNewKeys.add(name);
	}

	
	@Override
	public void removeValue(String name) {
	 removeAttribute(name);
	}

	@Override
	public void invalidate() {
		this.isValid = true;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}
	
	public void finish() {
		attributesAddNew.clear();
		attributesRemoveNewKeys.clear();
	}
	
}
