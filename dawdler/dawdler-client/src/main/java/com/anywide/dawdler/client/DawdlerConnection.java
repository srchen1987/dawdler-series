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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.client.net.aio.handler.ConnectorHandler;
import com.anywide.dawdler.client.net.aio.session.SocketSession;
import com.anywide.dawdler.core.compression.strategy.CompressionWrapper;
import com.anywide.dawdler.core.compression.strategy.ThresholdCompressionStrategy;
import com.anywide.dawdler.core.handler.IoHandler;
import com.anywide.dawdler.core.handler.IoHandlerFactory;
import com.anywide.dawdler.core.net.buffer.PoolBuffer;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.serializer.Serializer;

/**
 * 
 * @Title: DawdlerConnection.java
 * @Description: dawdler连接
 * @author: jackson.song
 * @date: 2015年03月16日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class DawdlerConnection {
	private static Logger logger = LoggerFactory.getLogger(DawdlerConnection.class);
	private final AtomicInteger TNUMBER = new AtomicInteger(0);
	private final AtomicLong INDEX = new AtomicLong(0);
	private String groupName;
	private int serializer;
	private int sessionNum;
	private String path;
	private String gid;
	private String user;
	private String password;
	private ConnectManager connectManager = new ConnectManager();
	private ConcurrentHashMap<SocketAddress, List<SocketSession>> sessionGroup = new ConcurrentHashMap<>();
	AsynchronousChannelGroup asynchronousChannelGroup;
	private ScheduledExecutorService reconnectScheduled;
	private static ConnectorHandler connectorHandler = new ConnectorHandler();
//	private AtomicBoolean connected= new AtomicBoolean();
	private AtomicBoolean complete = new AtomicBoolean();
	protected Semaphore semaphore = new Semaphore(0);
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	private IoHandler ioHandler = IoHandlerFactory.getHandler();

	public Semaphore getSemaphore() {
		return semaphore;
	}

	public AtomicBoolean getComplete() {
		return complete;
	}

	public ConnectManager getConnectManager() {
		return connectManager;
	}

	public ConcurrentHashMap<SocketAddress, List<SocketSession>> getSessionGroup() {
		return sessionGroup;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getSerializer() {
		return serializer;
	}

	public void setSerializer(int serializer) {
		this.serializer = serializer;
	}

	public String getGroupName() {
		return groupName;
	}

	private String getDefaultGroupName() {
		String host = "localhost";
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			host = "UnknowHost";
		}

		return host + "#ID:" + UUID.randomUUID().toString();
	}

	public DawdlerConnection(String gid/* ,String [] addresses */, String path, int serializer, int sessionNum,
			String user, String password) throws IOException {
		this.gid = gid;
//		this.addresses = addresses;
		this.path = path;
		this.groupName = getDefaultGroupName();
		this.sessionNum = sessionNum;
		this.serializer = serializer;
		this.user = user;
		this.password = password;
		asynchronousChannelGroup = AsynchronousChannelGroup
				.withThreadPool(Executors.newFixedThreadPool((Runtime.getRuntime().availableProcessors() * 2),
						(r) -> new Thread(r, "dawdler-Client-connector#" + gid + "#" + (TNUMBER.incrementAndGet()))));
		reconnectScheduled = Executors.newScheduledThreadPool(1);
		startReconnect();
	}

	public void startReconnect() {
		reconnectScheduled.scheduleWithFixedDelay(() -> {
			Set<SocketAddress> disconnAddressList = connectManager.getDisconnectAddress();
			if (disconnAddressList.isEmpty())
				return;
			Lock lock = rwlock.writeLock();
			try {
				lock.lock();
				boolean activate = false;
				for (final SocketAddress socketAddress : disconnAddressList) {
					Integer num = connectManager.removeDisconnect(socketAddress).get();
					if (num == 0) {
						continue;
					}
					logger.info("reconnect:" + socketAddress + ":" + num);
					DawdlerConnection.this.connect(socketAddress, num);
					activate = true;
				}
				if (activate)
					DawdlerConnection.this.rebuildSessionGroup();
			} finally {
				lock.unlock();
			}
		}, 5000, 3000, TimeUnit.MILLISECONDS);
	}

	public void connect(SocketAddress address) {
		connect(address, sessionNum);
	}

	public void connect(SocketAddress address, int sessionNum) {
		for (int i = 0; i < sessionNum; i++) {
			AsynchronousSocketChannel client = null;
			SocketSession socketSession = null;
			try {
				client = AsynchronousSocketChannel.open(asynchronousChannelGroup);
				// config(client);
				socketSession = new SocketSession(client, false);
				socketSession.setGroupName(getGroupName());
				socketSession.setDawdlerConnection(this);
				socketSession.setRemoteAddress(address);
				socketSession.setUser(user);
				socketSession.setPassword(password);
				socketSession.setPath(path);
			} catch (Exception e) {
				logger.error("", e);
				if (socketSession != null)
					socketSession.close(false);
			}
			if (client != null) {
				client.connect(address, socketSession, connectorHandler);
				try {
					socketSession.getInitLatch().await();
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
		}
	}

	public void connect(SocketAddress... addresses) {
		if (addresses == null || addresses.length == 0) {
			throw new IllegalArgumentException("addresses can not be null or empty!");
		}
		Set<SocketAddress> addressSet = new HashSet<SocketAddress>();
		for (SocketAddress socketAddress : addresses) {
			addressSet.add(socketAddress);
		}

		Set<SocketAddress> connectedAddressSet = sessionGroup.keySet();
		addressSet.removeAll(connectedAddressSet);
		Set<SocketAddress> disconnectedAddressSet = connectManager.getDisconnectAddress();
		addressSet.removeAll(disconnectedAddressSet);
		for (SocketAddress address : addressSet) {
			connect(address);
		}
//		rebuildSessionGroup();
	}

	public void refreshConnection(String... addresses) {
		refreshConnection(toSocketAddresses(addresses));
	}

	public void addConnection(String address) {
		connect(toSocketAddress(address));
	}

	public void disConnection(String address) {
		shutdown(toSocketAddress(address));
	}

	public SocketAddress[] toSocketAddresses(String... addresses) {
		if (addresses == null || addresses.length == 0) {
			throw new IllegalArgumentException("addresses can not be null or empty!");
		}
		SocketAddress[] socketAddresses = new SocketAddress[addresses.length];
		for (int i = 0; i < addresses.length; i++) {
			socketAddresses[i] = toSocketAddress(addresses[i]);
		}
		return socketAddresses;
	}

	public SocketAddress toSocketAddress(String address) {
		if (address == null || address.trim().equals("")) {
			throw new IllegalArgumentException("address can not be null or empty!");
		}
		String[] s = address.split(":");

		int index = address.lastIndexOf(":");
		if (index <= 0) {
			throw new IllegalArgumentException("address[" + address + "] is not a compliant rule!");
		}
		String ip = address.substring(0, index);
		String port = address.substring(index + 1, address.length());
		SocketAddress socketAddress = new InetSocketAddress(ip, Integer.parseInt(port));
		return socketAddress;
	}

	public void refreshConnection(SocketAddress... addressArray) {
		if ((addressArray == null || addressArray.length == 0)) {
			return;
		}
		List<SocketAddress> newAddresses = Arrays.asList(addressArray);
		List<SocketAddress> addList = new ArrayList<SocketAddress>();
		List<SocketAddress> removeList = new ArrayList<SocketAddress>();
		Set<SocketAddress> connectedAddresses = sessionGroup.keySet();
		Set<SocketAddress> disconnAddressList = connectManager.getDisconnectAddress();

		for (SocketAddress socketAddress : addressArray) {
			if (!connectedAddresses.contains(socketAddress) && !disconnAddressList.contains(socketAddress)) {
				addList.add(socketAddress);
			}
		}
		for (SocketAddress connectedAddress : connectedAddresses) {
			if (!newAddresses.contains(connectedAddress)) {
				removeList.add(connectedAddress);
			}
		}
		for (SocketAddress disconnectedAddress : disconnAddressList) {
			if (!newAddresses.contains(disconnectedAddress)) {
				removeList.add(disconnectedAddress);
			}
		}

		if (addList.size() > 0) {
			SocketAddress[] addresses = new SocketAddress[addList.size()];
			addList.toArray(addresses);
			connect(addresses);
		}

		for (SocketAddress socketAddress : removeList) {
			logger.info("remove" + socketAddress);
			shutdown(socketAddress);
		}

	}

	public void shutdown(SocketAddress socketAddress) {
		List<SocketSession> list = sessionGroup.remove(socketAddress);
		boolean shutdownNow = true;
		if (list != null) {
			connectManager.removeDisconnect(socketAddress);
			rebuildSessionGroup();
			for (SocketSession session : list) {
				if (!session.getFutures().isEmpty()) {
					session.markClose();
					shutdownNow = false;
				} else {
					session.close(false);
				}
			}
			if (sessionGroup.isEmpty()) {
				ConnectionPool.getConnectionPool(gid).remove(this);
				reconnectScheduled.shutdown();
				if (shutdownNow)
					try {
						asynchronousChannelGroup.shutdownNow();
					} catch (IOException e) {
						logger.error("", e);
					}
				else {
					asynchronousChannelGroup.shutdown();
				}
			}
		}
	}

	public void shutdownAll() {
		Enumeration<SocketAddress> addresses = sessionGroup.keys();
		while (addresses.hasMoreElements()) {
			SocketAddress ad = addresses.nextElement();
			shutdown(ad);
		}
	}

	private List<SocketSession>[] socketSessionList = new ArrayList[0];

	public void rebuildSessionGroup() {
		socketSessionList = sessionGroup.values().toArray(new ArrayList[0]);
	}

	public SocketSession getSession() {
		Lock lock = rwlock.readLock();
		try {
			lock.lock();
			if (socketSessionList.length == 0)
				return null;
			long index = Math.abs(INDEX.getAndIncrement());
			int position = (int) (index % socketSessionList.length);
			List<SocketSession> sessionList = socketSessionList[position];
			while (true) {
				if (sessionList == null || sessionList.isEmpty()) {
					if (socketSessionList.length > 0) {
						sessionList = socketSessionList[(int) (++index % socketSessionList.length)];
						continue;
					}
					return null;
				}
				position = (int) (index % sessionList.size());
				SocketSession socketSession = sessionList.get(position);
				if (socketSession.isClose()) {
					// sessionList.remove(socketSession);
					// FIXME tolog
					sessionList = null;
					rebuildSessionGroup();
					continue;
				}
				return socketSession;
			}
		} finally {
			lock.unlock();
		}
	}

	public void writeFirst(String path, Object obj, SocketSession socketSession) throws Exception {
		if (ioHandler != null)
			ioHandler.messageSent(socketSession, obj);
		Serializer serializer = SerializeDecider.decide((byte) this.serializer);
		byte[] datas = serializer.serialize(obj);
		CompressionWrapper cr = ThresholdCompressionStrategy.staticSingle().compress(datas);
		datas = cr.getBuffer();
		synchronized (socketSession) {
			ByteBuffer bf = socketSession.getWriteBuffer();
			PoolBuffer pb = null;
			byte[] pathBytes = path.getBytes();
			byte pathLength = (byte) pathBytes.length;
			int size = datas.length + 2 + pathLength;
			int capacity = size + 4;
			try {
				if (capacity > SocketSession.CAPACITY) {
					pb = PoolBuffer.selectPool(capacity);
					if (pb == null) {
						bf = ByteBuffer.allocate(capacity);
						logger.warn("The serialized object(" + obj.getClass().getName() + ") is too large.\t size :"
								+ capacity);
					} else
						bf = pb.getByteBuffer();
				}
				bf.putInt(size);
				int head = cr.isCompressed() ? this.serializer << 1 | 1 : this.serializer << 1;
				bf.put((byte) head);
				bf.put(pathLength);
				bf.put(pathBytes);
				bf.put(datas);
				bf.flip();
				socketSession.write(bf);
			} finally {
				bf.clear();
				if (pb != null)
					pb.release(bf);
			}
		}
	}

	public void write(Object obj, SocketSession socketSession) throws Exception {
		if (ioHandler != null)
			ioHandler.messageSent(socketSession, obj);
		Serializer serializer = SerializeDecider.decide((byte) this.serializer);
		byte[] datas = serializer.serialize(obj);
		CompressionWrapper cr = ThresholdCompressionStrategy.staticSingle().compress(datas);
		datas = cr.getBuffer();
		synchronized (socketSession) {
			ByteBuffer bf = socketSession.getWriteBuffer();
			PoolBuffer pb = null;
			int size = datas.length + 1;
			int capacity = size + 4;
			try {
				if (capacity > SocketSession.CAPACITY) {
					pb = PoolBuffer.selectPool(capacity);
					if (pb == null) {
						bf = ByteBuffer.allocate(capacity);
						logger.warn("The serialized object(" + obj.getClass().getName() + ") is too large.\t size :"
								+ capacity);
					} else
						bf = pb.getByteBuffer();
				}
				bf.putInt(size);
				int head = cr.isCompressed() ? this.serializer << 1 | 1 : this.serializer << 1;
				bf.put((byte) head);
				bf.put(datas);
				bf.flip();
				socketSession.write(bf);
			} finally {
				bf.clear();
				if (pb != null)
					pb.release(bf);
			}
		}
	}

	public void config(AsynchronousSocketChannel channel) {
		// NOOP
		try {
			channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		} catch (IOException e) {
			logger.error("", e);
		}

		try {
			channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		} catch (IOException e) {
			logger.error("", e);
		}
		try {
			channel.setOption(StandardSocketOptions.SO_SNDBUF, 16 * 1024);
		} catch (IOException e) {
			logger.error("", e);
		}

		try {
			channel.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
		} catch (IOException e) {
			logger.error("", e);
		}
	}

}
