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
package club.dawdler.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import club.dawdler.util.HashedWheelTimerSingleCreator;

/**
 * @author jackson.song
 * @version V1.0
 * 客户端连接池
 */
public class ConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	private static final ConcurrentHashMap<String, ConnectionPool> GROUPS = new ConcurrentHashMap<>();
	private static final Map<String, ServerChannelGroup> SERVER_CHANNEL_GROUPS = new HashMap<>();
	private List<DawdlerConnection> connections = new CopyOnWriteArrayList<>();
	private String groupName;

	public enum Action {
		ACTION_ADD, ACTION_DEL
	}

	private ConnectionPool() {
	}

	public static void addServerChannelGroup(String gid, ServerChannelGroup serverChannelGroup) {
		SERVER_CHANNEL_GROUPS.put(gid, serverChannelGroup);
		ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
		if (cp == null) {
			cp = new ConnectionPool();
			cp.groupName = gid;
			addGroup(gid, cp);
			if (serverChannelGroup.getHost() != null && !serverChannelGroup.getHost().equals("")) {
				cp.addConnection(gid, serverChannelGroup.getHost());
			}
		}
	}

	public static void initConnection(String gid) {
		ServerChannelGroup sg = SERVER_CHANNEL_GROUPS.get(gid);
		if (sg == null) {
			throw new NullPointerException("not configure " + gid + "!");
		}
		ConnectionPool cp = getConnectionPool(gid);
		String user = sg.getUser();
		String password = sg.getPassword();
		int connectionNum = sg.getConnectionNum();
		int serializer = sg.getSerializer();
		int sessionNum = sg.getSessionNum();
		if (connectionNum <= 0) {
			connectionNum = 1;
		}
		if (sessionNum <= 0) {
			sessionNum = 1;
		}
		for (int j = 0; j < connectionNum; j++) {
			DawdlerConnection dc;
			try {
				dc = new DawdlerConnection(gid, serializer, sessionNum, user, password);
				cp.add(dc);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

	public static ConnectionPool getConnectionPool(String groupName) {
		return GROUPS.get(groupName);
	}

	public static ConnectionPool addGroup(String groupName, ConnectionPool cp) {
		return GROUPS.putIfAbsent(groupName, cp);
	}

	/**
	 * provider for web container call
	 **/
	public static void shutdown() throws Exception {
		for (ConnectionPool c : GROUPS.values()) {
			c.close();
		}
		HashedWheelTimerSingleCreator.getHashedWheelTimer().stop();
		GROUPS.clear();

	}

	public void add(DawdlerConnection dawdlerConnection) {
		connections.add(dawdlerConnection);
	}

	public void remove(DawdlerConnection dawdlerConnection) {
		connections.remove(dawdlerConnection);
	}

	public List<DawdlerConnection> getConnections() {
		if (connections.isEmpty()) {
			throw new IllegalArgumentException("not find " + groupName + " provider!");
		}
		return connections;
	}

	public boolean hasConnection(){
		return !connections.isEmpty();
	}

	public void close() {
		connections.forEach(con -> {
			con.shutdownAll();
		});
		connections.clear();
	}

	public void addConnection(String gid, String ipaddress) {
		synchronized (this) {
			if (connections.isEmpty()) {
				initConnection(gid);
			}
		}
		connections.forEach(con -> {
			con.addConnection(ipaddress);
		});
	}

	public void delConnection(String ipaddress) {
		connections.forEach(con -> {
			con.disConnection(ipaddress);
		});
	}

	public void doChange(String gid, Action action, String address) {
		switch (action) {
		case ACTION_DEL: {
			delConnection(address);
			break;
		}
		case ACTION_ADD: {
			addConnection(gid, address);
			break;
		}
		default:
			break;
		}

	}

}
