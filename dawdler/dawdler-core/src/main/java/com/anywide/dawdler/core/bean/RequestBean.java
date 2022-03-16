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
package com.anywide.dawdler.core.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RequestBean.java
 * @Description 远程请求信息类
 * @date 2007年11月15日
 * @email suxuan696@gmail.com
 */
public class RequestBean implements Serializable {
	private static final long serialVersionUID = 2432958149343667660L;
	private long seq;
	private String serviceName;
	private String methodName;
	private Class<?>[] types;
	private Object[] args;
	private boolean fuzzy;
	private Map<String, Object> attachments;
	private boolean async;

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isFuzzy() {
		return fuzzy;
	}

	public void setFuzzy(boolean fuzzy) {
		this.fuzzy = fuzzy;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getTypes() {
		return types;
	}

	public void setTypes(Class<?>[] types) {
		this.types = types;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object... args) {
		this.args = args;
	}

	public void setAttachment(String key, Object value) {
		checkIfNullCreateAttachment();
		attachments.put(key, value);
	}

	public void setAttachmentIfAbsent(String key, Object value) {
		checkIfNullCreateAttachment();
		attachments.putIfAbsent(key, value);
	}

	public void setAttachments(Map<String, Object> attachments) {
		this.attachments = attachments;
	}

	public Object getAttachment(String key) {
		return attachments != null ? attachments.get(key) : null;
	}

	public Map<String, Object> getAttachments() {
		return attachments;
	}

	private void checkIfNullCreateAttachment() {
		if (attachments == null) {
			attachments = new HashMap<String, Object>();
		}
	}

	@Override
	public String toString() {
		return serviceName + "\t" + methodName + "\t" + seq;
	}
}
