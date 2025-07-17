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
package club.dawdler.core.net.aio.session;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.handler.IoHandler;
import club.dawdler.core.handler.IoHandlerFactory;
import club.dawdler.core.net.buffer.BufferFactory;
import club.dawdler.core.net.buffer.DawdlerByteBuffer;
import club.dawdler.core.serializer.Serializer;
import club.dawdler.core.thread.InvokeFuture;
import club.dawdler.util.HashedWheelTimerSingleCreator;
import club.dawdler.util.JVMTimeProvider;
import club.dawdler.util.Timeout;
import club.dawdler.util.TimerTask;

/**
 * @author jackson.song
 * @version V1.0
 * 抽象session类 提供读写超时重连、心跳处理等方式。
 */
public abstract class AbstractSocketSession {
	public static final  int CAPACITY = 1024 * 64;
	private static final Logger logger = LoggerFactory.getLogger(AbstractSocketSession.class);
	private static final long WRITER_IDLE_TIMEMILLIS = 8000;
	private static final long READER_IDLE_TIMEMILLIS = WRITER_IDLE_TIMEMILLIS * 15;
	protected final AsynchronousSocketChannel channel;
	private final Object writeLock = new Object();
	private final CountDownLatch sessionInitLatch = new CountDownLatch(1);
	private final AtomicLong sequence = new AtomicLong(0);
	protected SocketAddress remoteAddress;
	protected SocketAddress localAddress;
	protected String describe;
	protected int remotePort;
	protected volatile long lastReadTime;
	protected volatile long lastWriteTime;
	protected Timeout readerIdleTimeout;
	protected Timeout writerIdleTimeout;
	protected DawdlerByteBuffer readBuffer;
	protected DawdlerByteBuffer writeBuffer;
	protected int dataLength;
	protected int packageSize;
	protected int alreadyRead;
	protected byte[] appendData;
	protected boolean compress;
	protected String path;
	protected Serializer serializer;
	protected boolean needNext;
	protected AtomicBoolean close = new AtomicBoolean();
	protected AtomicBoolean markClose = new AtomicBoolean();
	protected byte headData;
	protected int position;
	protected Map<Long, InvokeFuture<Object>> futures = new ConcurrentHashMap<>();
	protected IoHandler ioHandler = IoHandlerFactory.getHandler();
	private String groupName;
	private boolean authored;
	private SessionState state = SessionState.RECEIVE;
	private boolean server;

	protected AbstractSocketSession(AsynchronousSocketChannel channel, boolean server) throws Exception {
		this.channel = channel;
		if (server) {
			this.server = server;
			init();
		}
	}

	public boolean isAuthored() {
		return authored;
	}

	public void setAuthored(boolean authored) {
		this.authored = authored;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Map<Long, InvokeFuture<Object>> getFutures() {
		return futures;
	}

	public void setFutures(Map<Long, InvokeFuture<Object>> futures) {
		this.futures = futures;
	}

	public DawdlerByteBuffer getWriteBuffer() {
		return writeBuffer;
	}

	public abstract void parseHead(ByteBuffer buffer);

	public int getHeadData() {
		return headData;
	}

	public CountDownLatch getInitLatch() {
		return sessionInitLatch;
	}

	public void init() throws Exception {
		remoteAddress = channel.getRemoteAddress();
		localAddress = channel.getLocalAddress();
		describe = "local:" + localAddress + " remote:" + remoteAddress + " hashCode/" + hashCode();
		remotePort = ((InetSocketAddress) remoteAddress).getPort();
		readBuffer = BufferFactory.createDirectBuffer(CAPACITY);
		writeBuffer = BufferFactory.createDirectBuffer(CAPACITY);
		writerIdleTimeout = HashedWheelTimerSingleCreator.getHashedWheelTimer().newTimeout(new WriterIdleTimeoutTask(),
				WRITER_IDLE_TIMEMILLIS, TimeUnit.MILLISECONDS);
		readerIdleTimeout = HashedWheelTimerSingleCreator.getHashedWheelTimer().newTimeout(new ReaderIdleTimeoutTask(),
				READER_IDLE_TIMEMILLIS, TimeUnit.MILLISECONDS);
	}

	public SocketAddress getLocalAddress() {
		return localAddress;
	}

	public void clean(final DawdlerByteBuffer byteBuffer) {
		byteBuffer.close();
	}

	public abstract void close();

	public boolean isClose() {
		return close.get();
	}

	public boolean isReceived() {
		return state == SessionState.RECEIVE;
	}

	public boolean isConnected() {
		return state == SessionState.CONNECTION;
	}

	public void appendData(byte[] data) {
		if (data.length > 0) {
			System.arraycopy(data, 0, appendData, position, data.length);
			position += data.length;
		}

	}

	public byte[] getAppendData() {
		return appendData;
	}

	public void appendReadLength(int length) {
		alreadyRead += length;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public int getRemanentDataLength() {
		return packageSize - alreadyRead;
	}

	public int getPackageSize() {
		return packageSize;
	}

	public void setPackageSize(int packageSize) {
		this.packageSize = packageSize;
	}

	public void toConnectionState() {
		state = SessionState.CONNECTION;
	}

	public void toReceiveState() {
		state = SessionState.RECEIVE;
	}

	public void toPrepare() {
		toReceiveState();
		appendData = null;
		dataLength = 0;
		packageSize = 0;
		alreadyRead = 0;
		position = 0;
		serializer = null;
		compress = false;
		needNext = false;
	}

	public void clearBuffer(ByteBuffer buffer) {
		buffer.clear();
	}

	public DawdlerByteBuffer getReadBuffer() {
		return readBuffer;
	}

	public long getLastReadTime() {
		return lastReadTime;
	}

	public void setLastReadTime(long lastReadTime) {
		this.lastReadTime = lastReadTime;
	}

	public long getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(long lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	public AsynchronousSocketChannel getChannel() {
		return channel;
	}

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getDescribe() {
		return describe;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return describe + "\tlastRead: " + (JVMTimeProvider.currentTimeMillis() - lastReadTime) + "\tLastWrite: "
				+ (JVMTimeProvider.currentTimeMillis() - lastWriteTime);
	}

	public void markClose() {
		markClose.set(true);
	}

	public abstract void messageCompleted();

	public void sentHeartbeat() {
		ByteBuffer bf = ByteBuffer.allocate(4);
		bf.putInt(0);
		bf.flip();
		write(bf);
	}

	public void write(ByteBuffer obj) {
		setLastWriteTime(JVMTimeProvider.currentTimeMillis());
		synchronized (this) {
			try {
				while (obj.hasRemaining()) {
					Future<Integer> future = channel.write(obj);
					future.get(1000, TimeUnit.MILLISECONDS);
				}
			} catch (Exception e) {
				logger.error("", e);
				close();
			} finally {
				obj.clear();
				obj = null;
				// release buffer
			}
		}
	}

	public boolean isServer() {
		return server;
	}

	public Object getWriteLock() {
		return writeLock;
	}

	public boolean isNeedNext() {
		return needNext;
	}

	public void setNeedNext(boolean needNext) {
		this.needNext = needNext;
	}

	public long getSequence() {
		return sequence.incrementAndGet();
	}

	public enum SessionState {
		RECEIVE, CONNECTION, FAIL
	}

	private final class WriterIdleTimeoutTask implements TimerTask {
		@Override
		public void run(Timeout timeout) throws Exception {
			if (timeout.isCancelled() || isClose()) {
				return;
			}
			long currentTime = JVMTimeProvider.currentTimeMillis();
			long lastWriteTime = AbstractSocketSession.this.lastWriteTime;
			long nextDelay = WRITER_IDLE_TIMEMILLIS - (currentTime - lastWriteTime);
			if (nextDelay <= 0) {
				if (ioHandler != null) {
					ioHandler.channelIdle(AbstractSocketSession.this, SessionIdleType.WRITE);
				}
				AbstractSocketSession.this.writerIdleTimeout = timeout.timer().newTimeout(this, WRITER_IDLE_TIMEMILLIS,
						TimeUnit.MILLISECONDS);
				AbstractSocketSession.this.sentHeartbeat();
			} else {
				AbstractSocketSession.this.writerIdleTimeout = timeout.timer().newTimeout(this, nextDelay,
						TimeUnit.MILLISECONDS);
			}
		}
	}

	private final class ReaderIdleTimeoutTask implements TimerTask {
		@Override
		public void run(Timeout timeout) throws Exception {
			if (timeout.isCancelled() || isClose()) {
				return;
			}
			long currentTime = JVMTimeProvider.currentTimeMillis();
			long lastReadTime = AbstractSocketSession.this.lastReadTime;
			long nextDelay = READER_IDLE_TIMEMILLIS - (currentTime - lastReadTime);
			if (nextDelay <= 0) {
				if (ioHandler != null) {
					ioHandler.channelIdle(AbstractSocketSession.this, SessionIdleType.READ);
				}
				AbstractSocketSession.this.readerIdleTimeout = timeout.timer().newTimeout(this, READER_IDLE_TIMEMILLIS,
						TimeUnit.MILLISECONDS);
				AbstractSocketSession.this.close();
			} else {
				AbstractSocketSession.this.readerIdleTimeout = timeout.timer().newTimeout(this, nextDelay,
						TimeUnit.MILLISECONDS);
			}
		}

	}
}
