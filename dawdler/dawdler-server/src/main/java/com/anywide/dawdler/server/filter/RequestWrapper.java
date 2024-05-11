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
package com.anywide.dawdler.server.filter;

import java.net.SocketAddress;
import java.util.Map;

import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.service.bean.ServicesBean;
import com.anywide.dawdler.core.service.processor.ServiceExecutor;
import com.anywide.dawdler.server.net.aio.session.SocketSession;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RequestWrapper.java
 * @Description request的包装类
 * @date 2015年4月8日
 * @email suxuan696@gmail.com
 */
public class RequestWrapper extends RequestBean {
	private static final long serialVersionUID = 2807385594696214109L;
	private final RequestBean request;
	private final ServicesBean service;
	private final ServiceExecutor serviceExecutor;
	private final SocketSession session;

	public RequestWrapper(RequestBean request, ServicesBean service, ServiceExecutor serviceExecutor,
			SocketSession session) {
		this.request = request;
		this.service = service;
		this.serviceExecutor = serviceExecutor;
		this.session = session;
	}

	@Override
	public boolean isFuzzy() {
		return request.isFuzzy();
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

	public ServiceExecutor getServiceExecutor() {
		return serviceExecutor;
	}

	public ServicesBean getService() {
		return service;
	}

	RequestBean getRequest() {
		return request;
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
