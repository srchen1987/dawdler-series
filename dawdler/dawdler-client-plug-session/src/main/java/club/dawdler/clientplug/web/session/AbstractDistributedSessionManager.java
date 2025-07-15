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

import club.dawdler.clientplug.web.session.http.DawdlerHttpSession;

import jakarta.servlet.http.HttpSessionListener;

/**
 * @author jackson.song
 * @version V1.0
 * 抽象分布式session管理器
 */
public abstract class AbstractDistributedSessionManager {
	public static final String DISTRIBUTED_SESSION_HTTP_SESSION_LISTENER = "DISTRIBUTED_SESSION_HTTP_SESSION_LISTENER";
	/**
	 * session监听器 目前只监听 创建session 销毁session
	 */
	protected HttpSessionListener httpSessionListener;

	public HttpSessionListener getHttpSessionListener() {
		return httpSessionListener;
	}

	public void setHttpSessionListener(HttpSessionListener httpSessionListener) {
		this.httpSessionListener = httpSessionListener;
	}

	public abstract DawdlerHttpSession getSession(String sessionKey);

	public abstract void close();

	public abstract void removeSession(String sessionKey);

	public abstract void removeSession(DawdlerHttpSession dawdlerHttpSession);

	public abstract void addSession(String sessionKey, DawdlerHttpSession dawdlerHttpSession);

	public abstract void invalidateAll();

	public abstract void addIpToBlacklist(String ip);

	public abstract boolean getIpBlack(String ip);

}
