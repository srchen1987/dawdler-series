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
package com.anywide.dawdler.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.client.conf.ClientConfig;
import com.anywide.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.client.discoverycenter.ZkDiscoveryCenterClient;
import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.util.HashedWheelTimerSingleCreator;

/**
 * 
 * @Title: ConnectionPool.java
 * @Description: 客户端连接池
 * @author: jackson.song
 * @date: 2015年03月16日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */

public class ConnectionPool {
	private static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	private static ConcurrentHashMap<String, ConnectionPool> groups = new ConcurrentHashMap<>();
	private static Map<String, ServerChannelGroup> serverChannelGroup = new HashMap<String, ServerChannelGroup>();
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	private static DiscoveryCenter discoveryCenter = null;
	static {
		try {
			ClientConfig clientConfig = ClientConfigParser.getClientConfig();
			String connectString = clientConfig.getZkHost();
			discoveryCenter = new ZkDiscoveryCenterClient(connectString, null, null);
			List<ServerChannelGroup> sgs = clientConfig.getServerChannelGroups();
			if (sgs != null) {
				for (ServerChannelGroup sg : sgs) {
					String gid = sg.getGroupId();
					serverChannelGroup.put(gid, sg);
					List<String> addresses = null;
					try {
						addresses = discoveryCenter.getServiceList(gid);
					} catch (Exception e) {
						logger.error("", e);
						continue;
					}
					if (addresses == null || addresses.isEmpty())
						continue;
					// gid get addresses;
					ConnectionPool cp = getConnectionPool(gid);
					if (cp == null) {
						cp = new ConnectionPool();
						ConnectionPool pre = addGroup(gid, cp);
						if (pre != null)
							cp = pre;
					}
					initConnection(gid);
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}

	}

	public static void initConnection(String gid) {
		ServerChannelGroup sg = serverChannelGroup.get(gid);
		if (sg == null)
			throw new NullPointerException("not configure " + gid + "!");
		ConnectionPool cp = getConnectionPool(gid);
		String path = sg.getPath();
		String user = sg.getUser();
		String password = sg.getPassword();
		int connectionNum = sg.getConnectionNum();
		int serializer = sg.getSerializer();
		int sessionNum = sg.getSessionNum();
		if (connectionNum <= 0)
			connectionNum = 1;
		if (sessionNum <= 0)
			sessionNum = 1;
		for (int j = 0; j < connectionNum; j++) {
			DawdlerConnection dc;
			try {
				dc = new DawdlerConnection(gid, path, serializer, sessionNum, user, password);
				cp.add(dc);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

	public static ConnectionPool getConnectionPool(String groupName) {
		return groups.get(groupName);
	}

	public static ConnectionPool addGroup(String groupName, ConnectionPool cp) {
		return groups.putIfAbsent(groupName, cp);
	}

	// provider for web container call
	public static void shutdown() throws Exception {
		for (ConnectionPool c : groups.values()) {
			c.close();
		}
		discoveryCenter.destroy();
		groups.clear();
		HashedWheelTimerSingleCreator.getHashedWheelTimer().stop();
	}

	public CircularQueue<DawdlerConnection> cq = new CircularQueue<>();

	public void add(DawdlerConnection dawdlerConnection) {
		cq.add(dawdlerConnection);
	}

	public void remove(DawdlerConnection dawdlerConnection) {
		cq.remove(dawdlerConnection);
	}

	public DawdlerConnection getConnection() {
		DawdlerConnection con = cq.get();
		if (!con.getComplete().get()) {
			try {
				con.semaphore.acquire();
			} catch (InterruptedException e) {
			}
		}
		return con;
	}

	public void close() {
		DawdlerConnection dc = null;
		while ((dc = cq.get()) != null) {
			cq.remove(dc);
			dc.shutdownAll();
		}
	}

	/**
	 * 
	 * @Title: updateConnection
	 * @Description: 废弃了 不需要之前的存储结构进行刷新连接了 之前结构/dawdler/gid/[provider,provider1]
	 *               如今换成这种格式，直接用add和del即可 /dawdler/gid/provider +provider1
	 *               +provider2
	 * @param ipaddresses
	 * @return: void
	 * @throws @author: jackson.song
	 * @date: 2018年8月13日
	 */
	@Deprecated
	public void updateConnection(String ipaddresses) {
		cq.refreshConnection(ipaddresses);
	}

	public void addConnection(String gid, String ipaddress) {
		if (cq.first == null) {
			initConnection(gid);
		}  
		cq.addConnection(ipaddress);
	}

	public void delConnection(String ipaddress) {
		cq.delConnection(ipaddress);
	}

	public void doChange(String gid, String action, String address) {
		switch (action) {
		case "del": {
			delConnection(address);
			break;
		}
		case "add": {
			addConnection(gid, address);
			break;
		}

//    以下是之前上个版本的 之后不需要通过删除一个gid的方式来关闭
//		case "del": {
//			ConnectionPool pool = groups.remove(groupid);
//			pool.close(); 
//			break;
//		}
//		   以下是之前上个版本的 之后不需要通过upate方式来更新节点了 只保留 add del
//		case "update": {
//		ConnectionPool2 pool = groups.get(groupid);
//		pool.updateConnection(addresses);
//		break;
//	}
		default:
			break;
		}

	}

	public class CircularQueue<T> {
		private volatile Node<T> first;
		private volatile Node<T> currentNode;

		public void add(T value) {
			Lock lock = rwlock.writeLock();
			try {
				lock.lock();
				if (first == null) {
					first = new Node<T>(value, null, null);
					this.currentNode = first;
				} else {
					Node<T> temp = new Node<T>(value, null, null);
					currentNode.next = temp;
					temp.pre = currentNode;
					currentNode = temp;
				}
			} finally {
				lock.unlock();
			}
		}

		public boolean exist(T value) {
			Node<T> temp = first;
			boolean exist = false;
			while ((temp != null) && !exist) {
				if (temp.value == value) {
					exist = true;
					break;
				}
				temp = temp.next;
			}
			return exist;
		}

		public void remove(T value) {
			Lock lock = rwlock.writeLock();
			try {
				lock.lock();
				Node<T> temp = first;
				boolean exist = false;
				while ((temp != null) && !exist) {
					if (temp.value == value) {
						exist = true;
						break;
					}
					temp = temp.next;
				}
				if (exist) {
					if (first.value == value) {
						currentNode = null;
						first = null;
						return;
					}
					if (temp.pre != null)
						temp.pre.next = temp.next;
					if (temp.next != null)
						temp.next.pre = temp.pre;
				}
			} finally {
				lock.unlock();
			}
		}

		public void refreshConnection(String addresses) {
			Lock lock = rwlock.writeLock();
			try {
				lock.lock();
				Node<T> temp = first;
				while (temp != null) {
					DawdlerConnection con = (DawdlerConnection) temp.value;
					con.refreshConnection(addresses.split(","));
					temp = temp.next;
				}
			} finally {
				lock.unlock();
			}
		}

		public void addConnection(String address) {
			Lock lock = rwlock.writeLock();
			try {
				lock.lock();
				Node<T> temp = first;
				while (temp != null) {
					DawdlerConnection con = (DawdlerConnection) temp.value;
					con.addConnection(address);
					temp = temp.next;
				}
			} finally {
				lock.unlock();
			}
		}

		public void delConnection(String address) {
			Lock lock = rwlock.writeLock();
			try {
				lock.lock();
				Node<T> temp = first;
				while (temp != null) {
					DawdlerConnection con = (DawdlerConnection) temp.value;
					con.disConnection(address);
					temp = temp.next;
				}
			} finally {
				lock.unlock();
			}
		}

		public T get() {
			Lock lock = rwlock.readLock();
			try {
				lock.lock();
				boolean exist = currentNode != null;
				if (exist) {
					Node<T> temp = null;
					try {
						temp = currentNode.next;
					} catch (Exception e) {
						return get();
					}
					currentNode = temp;
					if (temp != null) {
						return temp.value;
					}
					currentNode = temp = first;
					if (temp != null) {
						return temp.value;
					}
				}
				if (first == null)
					return null;
				return get();
			} finally {
				lock.unlock();
			}
		}

	}

	public class Node<T> {
		private T value;
		public Node<T> pre;
		public Node<T> next;

		public Node(T value, Node<T> pre, Node<T> next) {
			this.value = value;
			this.pre = pre;
			this.next = next;
		}

	}
}
