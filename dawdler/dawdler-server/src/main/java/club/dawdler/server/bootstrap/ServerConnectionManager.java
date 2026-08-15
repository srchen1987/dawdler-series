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
package club.dawdler.server.bootstrap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import club.dawdler.server.net.aio.session.SocketSession;

/**
 * @author jackson.song
 * @version V1.0
 * 服务器连接管理器
 */
public class ServerConnectionManager {
	private static final ServerConnectionManager serverConnectionManager = new ServerConnectionManager();
	public Set<SocketSession> connections = ConcurrentHashMap.newKeySet();

	private ServerConnectionManager() {

	}

	public static ServerConnectionManager getInstance() {
		return serverConnectionManager;
	}

	public void addSession(SocketSession session) {
		connections.add(session);
	}

	public boolean removeSession(SocketSession session) {
		return connections.remove(session);
	}

	public boolean hasTask() {
		for (SocketSession session : connections) {
			if (!session.getFutures().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void closeNow() {
		for (SocketSession session : connections) {
			session.close();
		}
	}

}
