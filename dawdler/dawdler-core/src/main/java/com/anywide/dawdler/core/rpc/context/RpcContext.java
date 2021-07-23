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
package com.anywide.dawdler.core.rpc.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RpcClientContext.java
 * @Description rpc上下文，可以在调用时获取RequestWrapper
 * @date 2020年04月22日
 * @email suxuan696@gmail.com
 */
public class RpcContext {

	private Map<String, Object> attachments;
	private static final ThreadLocal<RpcContext> THREAD_LOCAL = new ThreadLocal<RpcContext>() {
		@Override
		protected RpcContext initialValue() {
			return new RpcContext();
		}
	};

	public static RpcContext getContext() {
		return THREAD_LOCAL.get();
	}

	public static void removeContext() {
		THREAD_LOCAL.remove();
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

	

}
