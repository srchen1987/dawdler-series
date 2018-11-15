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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.client.conf.ClientConfig;
import com.anywide.dawdler.client.conf.ClientConfig.ServerChannelGroup;
import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.util.HashedWheelTimerSingleCreator;

/**
 * 
 * @Title:  ConnectionPool.java
 * @Description:    客户端连接池   
 * @author: jackson.song    
 * @date:   2015年03月16日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */

public class ConnectionPool {
	private static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	private static ConcurrentHashMap<String, ConnectionPool> groups = new ConcurrentHashMap<>();
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	Object lock = new Object();
	static {
		try {
			ClientConfig clientConfig = ClientConfigParser.getClientConfig();
			List<ServerChannelGroup> sgs = clientConfig.getServerChannelGroups();
			if (sgs != null) {
				for (ServerChannelGroup sg : sgs) {
					String gid = sg.getGroupId();
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
					// String addresses = "10.26.173.10:9527";
					String addresses = null;
					try {
						addresses = PropertiesCenter.getInstance().getValue(gid);
					} catch (Exception e) {
						logger.error("", e);
						continue;
					}
					if (addresses == null)
						continue;
					// gid get addresses;
					for (int j = 0; j < connectionNum; j++) {
						ConnectionPool cp = getConnectionPool(gid);
						if (cp == null) {
							cp = new ConnectionPool();
							addGroup(gid, cp);
						}
						DawdlerConnection dc;
						try {
							dc = new DawdlerConnection(gid,addresses.split(","), path, serializer, sessionNum, user, password);
							cp.add(dc);
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}

	}

	public static ConnectionPool getConnectionPool(String groupName) {
		return groups.get(groupName);
	}

	public static void addGroup(String groupName, ConnectionPool cp) {
		groups.put(groupName, cp);
	}

	public static void shutdown() {
		for (ConnectionPool c : groups.values()) {
			c.close();
		}
		PropertiesCenter.getInstance().close();
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
		if(!con.initconnected) {
			if(con.getConnected().compareAndSet(false, true)) {
				con.connect();
				con.initconnected=true;
			}
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

	public void updateConnection(String ipaddresses) {
		cq.refreshConnection(ipaddresses);
	}

	public void doChange(String action, String addresses, String groupid) {
		switch (action) {
		case "del": {
			ConnectionPool pool = groups.remove(groupid);
			pool.close();
			break;
		}
		case "update": {
			ConnectionPool pool = groups.get(groupid);
			pool.updateConnection(addresses);
		}
			break;
		default:
			break;
		}

	}

	public class CircularQueue<T> {
		private Node<T> first;
		private Node<T> currentNode;

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
					con.refreshConnect(addresses.split(","));
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
				synchronized (lock) {
					boolean exist = currentNode != null;
					if (exist) {
						Node<T> temp = currentNode.next;
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
				}
			} finally {
				lock.unlock();
			}

			// return null;
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
