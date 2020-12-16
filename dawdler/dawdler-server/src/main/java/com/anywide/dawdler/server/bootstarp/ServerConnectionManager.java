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
package com.anywide.dawdler.server.bootstarp;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.anywide.dawdler.server.net.aio.session.SocketSession;

/**
 * 
 * @Title: ServerConnectionManager.java
 * @Description: 服务器连接管理器
 * @author: jackson.song
 * @date: 2015年03月12日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ServerConnectionManager {
	private static ServerConnectionManager serverConnectionManager = new ServerConnectionManager();

	private ServerConnectionManager() {

	}

	public static ServerConnectionManager getInstance() {
		return serverConnectionManager;
	}

	public Map<SocketAddress, SocketSession> connections = new ConcurrentHashMap<>();


	public void addSession(SocketSession session) { 
		connections.put(session.getRemoteAddress(), session);
	}

	public boolean removeSession(SocketSession session) {
		return connections.remove(session.getRemoteAddress()) != null;
	}

	public boolean hasTask() {
		Collection<SocketSession> collection = connections.values();
		for (SocketSession session : collection) {
			if (!session.getFutures().isEmpty())
				return true;
		}
		return false;
	}

	public void closeNow() {
		Collection<SocketSession> collection = connections.values();
		for (SocketSession session : collection) {
			session.close();
		}
	}

	public void close() {
		Collection<SocketSession> collection = connections.values();
		for (SocketSession session : collection) {
			if (session.getFutures().isEmpty()) {
				session.close();
			}
		}
	}

}
