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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.net.aio.handler.ConnectorHandler;
import com.anywide.dawdler.client.net.aio.session.SocketSession;
import com.anywide.dawdler.core.compression.strategy.CompressionWrapper;
import com.anywide.dawdler.core.compression.strategy.ThresholdCompressionStrategy;
import com.anywide.dawdler.core.handler.IoHandler;
import com.anywide.dawdler.core.handler.IoHandlerFactory;
import com.anywide.dawdler.core.net.buffer.DawdlerByteBuffer;
import com.anywide.dawdler.core.net.buffer.PoolBuffer;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.serializer.Serializer;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerConnection.java
 * @Description dawdler连接
 * @date 2015年3月16日
 * @email suxuan696@gmail.com
 */
public class DawdlerConnection {
	private static final ConnectorHandler CONNECTOR_HANDLER = new ConnectorHandler();
	private static final Logger logger = LoggerFactory.getLogger(DawdlerConnection.class);
	private final AtomicInteger TNUMBER = new AtomicInteger(0);
	private final int sessionNum;
	private final String gid;
	private final String user;
	private final String password;
	private final ConnectManager connectManager = new ConnectManager();
	private final ConcurrentHashMap<SocketAddress, List<SocketSession>> sessionGroup = new ConcurrentHashMap<>();
	private final ScheduledExecutorService reconnectScheduled;
	private final AtomicBoolean complete = new AtomicBoolean();
	private final IoHandler ioHandler = IoHandlerFactory.getHandler();
	private DawdlerForkJoinWorkerThreadFactory dawdlerForkJoinWorkerThreadFactory = new DawdlerForkJoinWorkerThreadFactory();
	protected Semaphore semaphore = new Semaphore(0);
	AsynchronousChannelGroup asynchronousChannelGroup;
	private final String groupName;
	private int serializer;
	private List<List<SocketSession>> socketSessions;

	public DawdlerConnection(String gid, int serializer, int sessionNum, String user, String password)
			throws IOException {
		this.gid = gid;
		this.groupName = getDefaultGroupName();
		this.sessionNum = sessionNum;
		this.serializer = serializer;
		this.user = user;
		this.password = password;
		asynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(new ForkJoinPool(
				Runtime.getRuntime().availableProcessors() * 2, dawdlerForkJoinWorkerThreadFactory, null, true));
		reconnectScheduled = Executors.newScheduledThreadPool(1);
		startReconnect();
	}

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
			host = "UnknownHost";
		}

		return host + "#ID:" + UUID.randomUUID().toString();
	}

	public void startReconnect() {
		reconnectScheduled.scheduleWithFixedDelay(() -> {
			Set<SocketAddress> disconnAddressList = connectManager.getDisconnectAddress();
			if (disconnAddressList.isEmpty()) {
				return;
			}
			try {
				boolean activate = false;
				for (final SocketAddress socketAddress : disconnAddressList) {
					int num = connectManager.removeDisconnect(socketAddress).get();
					if (num == 0) {
						continue;
					}
					logger.info("reconnect:" + socketAddress + ":" + num);
					DawdlerConnection.this.connect(socketAddress, num);
					activate = true;
				}
				if (activate) {
					DawdlerConnection.this.rebuildSessionGroup();
				}

			} catch (Exception e) {
				logger.error("", e);
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
				socketSession = new SocketSession(client);
				socketSession.setGroupName(getGroupName());
				socketSession.setDawdlerConnection(this);
				socketSession.setRemoteAddress(address);
				socketSession.setUser(user);
				socketSession.setPassword(password);
				socketSession.setPath(gid);
			} catch (Exception e) {
				logger.error("", e);
				if (socketSession != null) {
					socketSession.close(false);
				}

			}
			if (client != null && socketSession != null) {
				client.connect(address, socketSession, CONNECTOR_HANDLER);
				try {
					socketSession.getInitLatch().await(5, TimeUnit.SECONDS);
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
		Set<SocketAddress> addressSet = new HashSet<>(Arrays.asList(addresses));
		Set<SocketAddress> connectedAddressSet = sessionGroup.keySet();
		addressSet.removeAll(connectedAddressSet);
		Set<SocketAddress> disconnectedAddressSet = connectManager.getDisconnectAddress();
		addressSet.removeAll(disconnectedAddressSet);
		for (SocketAddress address : addressSet) {
			connect(address);
		}
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
		int index = address.lastIndexOf(":");
		if (index <= 0) {
			throw new IllegalArgumentException("address[" + address + "] is not a compliant rule!");
		}
		String ip = address.substring(0, index);
		String port = address.substring(index + 1);
		return new InetSocketAddress(ip, Integer.parseInt(port));
	}

	public void refreshConnection(SocketAddress... addressArray) {
		if ((addressArray == null || addressArray.length == 0)) {
			return;
		}
		List<SocketAddress> newAddresses = Arrays.asList(addressArray);
		List<SocketAddress> addList = new ArrayList<>();
		List<SocketAddress> removeList = new ArrayList<>();
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
		List<SocketSession> sessions = sessionGroup.remove(socketAddress);
		boolean shutdownNow = true;
		if (sessions != null) {
			connectManager.removeDisconnect(socketAddress);
			rebuildSessionGroup();
			for (SocketSession session : sessions) {
				if (!session.getFutures().isEmpty()) {
					session.markClose();
					shutdownNow = false;
				} else {
					session.close(false);
				}
			}
			if (sessionGroup.isEmpty()) {
				ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
				if(cp != null) {
					cp.remove(this);
				}
				reconnectScheduled.shutdown();
				if (shutdownNow) {
					try {
						asynchronousChannelGroup.shutdownNow();
					} catch (IOException e) {
						logger.error("", e);
					}
				} else {
					asynchronousChannelGroup.shutdown();
				}
			}
		}
	}

	public void shutdownExecutors() {
		if (!reconnectScheduled.isShutdown()) {
			reconnectScheduled.shutdownNow();
		}
		if (!asynchronousChannelGroup.isShutdown()) {
			try {
				asynchronousChannelGroup.shutdownNow();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
	}

	public void shutdownAll() {
		Enumeration<SocketAddress> addresses = sessionGroup.keys();
		if (!addresses.hasMoreElements()) {
			shutdownExecutors();
			return;
		}
		while (addresses.hasMoreElements()) {
			SocketAddress ad = addresses.nextElement();
			shutdown(ad);
		}
	}

	public void rebuildSessionGroup() {
		socketSessions = new CopyOnWriteArrayList<>(sessionGroup.values());
	}

	public List<List<SocketSession>> getSessions() {
		return socketSessions;
	}

	public void writeFirst(String path, Object obj, SocketSession socketSession) throws Exception {
		if (ioHandler != null) {
			ioHandler.messageSent(socketSession, obj);
		}
		socketSession.setClassLoader(Thread.currentThread().getContextClassLoader());
		Serializer serializer = SerializeDecider.decide((byte) this.serializer);
		byte[] data = serializer.serialize(obj);
		CompressionWrapper cr = ThresholdCompressionStrategy.staticSingle().compress(data);
		data = cr.getBuffer();
		synchronized (socketSession) {
			DawdlerByteBuffer dawdlerByteBuffer = socketSession.getWriteBuffer();
			ByteBuffer byteBuffer = null;
			PoolBuffer poolBuffer = null;
			byte[] pathBytes = path.getBytes();
			byte pathLength = (byte) pathBytes.length;
			int size = data.length + 2 + pathLength;
			int capacity = size + 4;
			try {
				if (capacity > SocketSession.CAPACITY) {
					poolBuffer = PoolBuffer.selectPool(capacity);
					if (poolBuffer == null) {
						byteBuffer = ByteBuffer.allocate(capacity);
						logger.warn("The serialized object(" + obj.getClass().getName() + ") is too large.\t size :"
								+ capacity);
					} else {
						dawdlerByteBuffer = poolBuffer.getByteBuffer();
						byteBuffer = dawdlerByteBuffer.getByteBuffer();
					}
				} else {
					byteBuffer = dawdlerByteBuffer.getByteBuffer();
				}
				byteBuffer.putInt(size);
				int head = cr.isCompressed() ? this.serializer << 1 | 1 : this.serializer << 1;
				byteBuffer.put((byte) head);
				byteBuffer.put(pathLength);
				byteBuffer.put(pathBytes);
				byteBuffer.put(data);
				byteBuffer.flip();
				socketSession.write(byteBuffer);
			} finally {
				byteBuffer.clear();
				if (poolBuffer != null) {
					poolBuffer.release(dawdlerByteBuffer);
				}

			}
		}
	}

	public void write(Object obj, SocketSession socketSession) throws Exception {
		if (ioHandler != null) {
			ioHandler.messageSent(socketSession, obj);
		}

		Serializer serializer = SerializeDecider.decide((byte) this.serializer);
		byte[] data = serializer.serialize(obj);
		CompressionWrapper cr = ThresholdCompressionStrategy.staticSingle().compress(data);
		data = cr.getBuffer();
		synchronized (socketSession) {
			DawdlerByteBuffer dawdlerByteBuffer = socketSession.getWriteBuffer();
			ByteBuffer byteBuffer = null;
			PoolBuffer poolBuffer = null;
			int size = data.length + 1;
			int capacity = size + 4;
			try {
				if (capacity > SocketSession.CAPACITY) {
					poolBuffer = PoolBuffer.selectPool(capacity);
					if (poolBuffer == null) {
						byteBuffer = ByteBuffer.allocate(capacity);
						logger.warn("The serialized object(" + obj.getClass().getName() + ") is too large.\t size :"
								+ capacity);
					} else {
						dawdlerByteBuffer = poolBuffer.getByteBuffer();
						byteBuffer = dawdlerByteBuffer.getByteBuffer();
					}
				} else {
					byteBuffer = dawdlerByteBuffer.getByteBuffer();
				}
				byteBuffer.putInt(size);
				int head = cr.isCompressed() ? this.serializer << 1 | 1 : this.serializer << 1;
				byteBuffer.put((byte) head);
				byteBuffer.put(data);
				byteBuffer.flip();
				socketSession.write(byteBuffer);
			} finally {
				byteBuffer.clear();
				if (poolBuffer != null) {
					poolBuffer.release(dawdlerByteBuffer);
				}

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

	static final class DawdlerForkJoinWorkerThread extends ForkJoinWorkerThread {
		protected DawdlerForkJoinWorkerThread(ForkJoinPool pool) {
			super(pool);
		}
	}

	final class DawdlerForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
		public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			ForkJoinWorkerThread thread = new DawdlerForkJoinWorkerThread(pool);
			thread.setName("dawdler-Client-connector#" + gid + "#" + (TNUMBER.incrementAndGet()));
			return thread;
		}
	}

}
