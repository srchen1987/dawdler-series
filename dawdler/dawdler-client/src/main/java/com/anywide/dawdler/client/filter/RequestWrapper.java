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
package com.anywide.dawdler.client.filter;

import java.net.SocketAddress;
import java.util.Map;

import com.anywide.dawdler.client.net.aio.session.SocketSession;
import com.anywide.dawdler.core.annotation.CircuitBreaker;
import com.anywide.dawdler.core.bean.RequestBean;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RequestWrapper.java
 * @Description 一个request的包装类
 * @date 2015年4月06日
 * @email suxuan696@gmail.com
 */
public class RequestWrapper extends RequestBean {
	private static final long serialVersionUID = 2807385594696214109L;
	private final SocketSession session;
	private final RequestBean request;
	private final int timeout;

	private final CircuitBreaker circuitBreaker;
	private final Class<?> proxyInterface;

	public RequestWrapper(RequestBean request, SocketSession session, CircuitBreaker circuitBreaker,
			Class<?> proxyInterface, int timeout) {
		this.timeout = timeout;
		this.request = request;
		this.session = session;
		this.circuitBreaker = circuitBreaker;
		this.proxyInterface = proxyInterface;
	}

	@Override
	public boolean isAsync() {
		return request.isAsync();
	}

	@Override
	public boolean isFuzzy() {
		return request.isFuzzy();
	}

	@Override
	public boolean isSingle() {
		return request.isSingle();
	}

	@Override
	public String getPath() {
		return request.getPath();
	}

	@Override
	public long getSeq() {
		return request.getSeq();
	}

	@Override
	public String getServiceName() {
		return request.getServiceName();
	}

	@Override
	public String getMethodName() {
		return request.getMethodName();
	}

	@Override
	public Class<?>[] getTypes() {
		return request.getTypes();
	}

	@Override
	public Object[] getArgs() {
		return request.getArgs();
	}

	@Override
	public Object getAttachment(String key) {
		return request.getAttachment(key);
	}

	@Override
	public Map<String, Object> getAttachments() {
		return request.getAttachments();
	}

	public Class<?> getProxyInterface() {
		return proxyInterface;
	}

	public CircuitBreaker getCircuitBreaker() {
		return circuitBreaker;
	}

	RequestBean getRequest() {
		return request;
	}

	SocketSession getSession() {
		return session;
	}

	public int getTimeout() {
		return timeout;
	}

	public SocketAddress getRemoteAddress() {
		return session.getRemoteAddress();
	}

	public SocketAddress getLocalAddress() {
		return session.getLocalAddress();
	}

	@Override
	public void setAttachment(String key, Object value) {
		request.setAttachment(key, value);
	}

	@Override
	public void setAttachmentIfAbsent(String key, Object value) {
		request.setAttachmentIfAbsent(key, value);
	}

	@Override
	public void setAttachments(Map<String, Object> attachments) {
		request.setAttachments(attachments);
	}
}
