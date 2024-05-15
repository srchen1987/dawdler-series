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
package com.anywide.dawdler.clientplug.web.session.store;

import java.util.Map;

import com.anywide.dawdler.clientplug.web.session.AbstractDistributedSessionManager;
import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;

/**
 * @author jackson.song
 * @version V1.0
 * @Title SessionStore.java
 * @Description session存储抽象接口
 * @date 2020年12月12日
 * @email suxuan696@gmail.com
 */
public interface SessionStore {

	void saveSession(DawdlerHttpSession session) throws Exception;

	void saveSession(DawdlerHttpSession session, String ip, AbstractDistributedSessionManager sessionManager,
			boolean defense, int ipLimit, int ipMaxInactiveInterval) throws Exception;

	Map<byte[], byte[]> getAttributes(String sessionKey) throws Exception;

	byte[] getAttribute(String sessionKey, String attribute) throws Exception;

	void removeSession(String sessionKey) throws Exception;

}
