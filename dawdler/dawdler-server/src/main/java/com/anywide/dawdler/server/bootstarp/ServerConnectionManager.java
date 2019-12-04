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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.anywide.dawdler.server.net.aio.session.SocketSession;
/**
 * 
 * @Title:  ServerConnectionManager.java
 * @Description:    服务器连接管理器   
 * @author: jackson.song    
 * @date:   2015年03月12日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class ServerConnectionManager {
	private static ServerConnectionManager serverConnectionManager = new ServerConnectionManager();
	private ServerConnectionManager(){
		
	}
	public static ServerConnectionManager getInstance() {
		return serverConnectionManager;
	}
	public ConcurrentHashMap<SocketAddress,ServerConnection> connections = new ConcurrentHashMap<>();
	
	public ServerConnection getServerConnection(SocketSession session) {
		ServerConnection serverConnection = new ServerConnection(); 
		ServerConnection pre = connections.putIfAbsent(session.getRemoteAddress(),serverConnection);
		if(pre!=null)serverConnection=pre;
		return serverConnection;
	}
	public boolean addSession(SocketSession session) {
		ServerConnection serverConnection = getServerConnection(session);
		synchronized(session.getRemoteAddress()) {
			return serverConnection.addSession(session);
		}
	}
	public boolean removeSession(SocketSession session) {
		ServerConnection serverConnection = getServerConnection(session);
		synchronized(session.getRemoteAddress()) {
			boolean b = serverConnection.removeSession(session);
			if(b&&serverConnection.connectionNums.get()==0)connections.remove(session.getRemoteAddress());
			return b;
		}
	}
	
	public boolean hasTask() {
		Collection<ServerConnection> collection = connections.values();
		for(ServerConnection serverConnection : collection) {
			if(serverConnection.isRunning())return true;
		}
		return false;
	}
	
	public void closeNow() {
		Collection<ServerConnection> collection = connections.values();
		for(ServerConnection serverConnection : collection) {
			if(!serverConnection.isRunning()) {
				serverConnection.closeNow();
			}
		}
	}
	public void close() {
		Collection<ServerConnection> collection = connections.values();
		for(ServerConnection serverConnection : collection) {
			if(!serverConnection.isRunning()) { 
				serverConnection.close();
			}
		}
	}
	
	public int getConnectionCount(SocketAddress socketAddress) {
		synchronized (socketAddress) {
			ServerConnection serverConnection = connections.get(socketAddress);
			if(serverConnection==null)return 0;
			return serverConnection.connectionNums.get();
		}
		
	}
	private static class ServerConnection{
		private AtomicInteger connectionNums = new AtomicInteger(0);
		private List<SocketSession> sessions = new ArrayList<SocketSession>(); 
		
		public synchronized boolean addSession(SocketSession  session) {
			boolean b = sessions.add(session);
			if(b)connectionNums.incrementAndGet();
			return b;
		}
		public synchronized boolean removeSession(SocketSession  session) {
			boolean b = sessions.remove(session);
			if(b)connectionNums.decrementAndGet();
			return b;
		}
		public synchronized boolean isRunning() {
			for(SocketSession session : sessions) {
				if(!session.getFutures().isEmpty())return true;
			}
			return false;
		}
		public synchronized void closeNow() {
			List<SocketSession> closeSessions = new ArrayList(); 
			for(SocketSession session : sessions) {
				closeSessions.add(session);
			}
			for(SocketSession session : closeSessions) {
				session.close();
			}
		}
		public synchronized void close() {
			List<SocketSession> closeSessions = new ArrayList(); 
			for(SocketSession session : sessions) {
				if(session.getFutures().isEmpty())closeSessions.add(session);
			}
			for(SocketSession session : closeSessions) {
				session.close();
			}
		}
	}

}
