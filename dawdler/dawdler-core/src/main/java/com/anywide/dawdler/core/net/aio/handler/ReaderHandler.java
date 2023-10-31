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
package com.anywide.dawdler.core.net.aio.handler;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.exception.IllegalConnectionException;
import com.anywide.dawdler.core.net.aio.session.AbstractSocketSession;
import com.anywide.dawdler.core.net.buffer.DawdlerByteBuffer;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ReaderHandler.java
 * @Description aio读包的处理者，相对比较复杂 实现粘包等功能
 * @date 2015年3月12日
 * @email suxuan696@gmail.com
 */
public class ReaderHandler implements CompletionHandler<Integer, AbstractSocketSession> {
	private final static Logger logger = LoggerFactory.getLogger(ReaderHandler.class);
	private static final int HEADER_FIELD_LENGTH = Integer.BYTES;
	private final AtomicInteger INFERIOR_COUNT = new AtomicInteger();
	private static final int AUTH_DATA_SIZE = 2048;
	private static final int INFERIOR_COUNT_NUM = 10;

	@Override
	public void completed(Integer result, AbstractSocketSession session) {
		if (result == -1) {
			session.close();
			return;
		}
		try {
			session.setLastReadTime(JVMTimeProvider.currentTimeMillis());
			if (result > 0) {
				DawdlerByteBuffer dawdlerBuffer = session.getReadBuffer();
				ByteBuffer buffer = dawdlerBuffer.getByteBuffer();
				if (session.isReceived()) {
					if (session.isNeedNext() ? buffer.remaining() <= HEADER_FIELD_LENGTH
							: buffer.position() <= HEADER_FIELD_LENGTH) {
						if (session.isNeedNext()) {
							byte[] data = new byte[buffer.remaining()];
							buffer.get(data);
							buffer.clear();
							buffer.put(data);
							session.setNeedNext(false);
						}
						process(session);
						return;
					}
					session.toConnectionState();
					if (!session.isNeedNext()) {
						buffer.flip();
					}
					int dataLength = buffer.getInt();
					InetSocketAddress inetAddress = (InetSocketAddress) session.getRemoteAddress();
					String ipAddress = inetAddress.getAddress().getHostAddress();
					if (session.isServer() && !session.isAuthored() && dataLength > AUTH_DATA_SIZE) {
						throw new IllegalConnectionException(
								ipAddress + " send auth data " + dataLength + "B > " + AUTH_DATA_SIZE + "B.", ipAddress,
								dataLength);
					}
					if (dataLength == 0) {
						if (buffer.remaining() > 0) {
							session.toReceiveState();
							session.setNeedNext(true);
							completed(buffer.remaining(), session);
						} else {
							session.clearBuffer(buffer);
							session.toPrepare();
							process(session);
						}
						return;
					}
					session.setDataLength(dataLength);
					session.setPackageSize(dataLength);
					int readLength = buffer.remaining();
					if (readLength > dataLength) {
						session.parseHead(buffer);
						session.appendReadLength(dataLength);
						buffer.get(session.getAppendData());
						session.messageCompleted();
						session.setNeedNext(true);
						completed(buffer.remaining(), session);
						return;
					} else if (buffer.remaining() == dataLength) {
						session.parseHead(buffer);
						session.appendReadLength(dataLength);
						buffer.get(session.getAppendData());
						session.clearBuffer(buffer);
						session.messageCompleted();
						process(session);
					} else {
						session.appendReadLength(readLength);
						session.parseHead(buffer);
						int remain = buffer.remaining();
						if (remain > 0) {
							byte[] data = new byte[remain];
							buffer.get(data);
							session.appendData(data);
						}
						session.clearBuffer(buffer);
						session.setNeedNext(false);
						process(session);
					}
				} else {
					buffer.flip();
					int readLength = buffer.remaining();
					int remanentDataLength = session.getRemanentDataLength();
					if (readLength > remanentDataLength) {
						byte[] data = new byte[remanentDataLength];
						buffer.get(data);
						session.appendReadLength(remanentDataLength);
						session.appendData(data);
						session.messageCompleted();
						session.setNeedNext(true);
						completed(buffer.remaining(), session);
						return;
					} else {
						if (readLength == remanentDataLength) {
							session.appendReadLength(remanentDataLength);
							byte[] data = new byte[remanentDataLength];
							buffer.get(data);
							session.appendData(data);
							session.clearBuffer(buffer);
							session.messageCompleted();
						} else {
							session.appendReadLength(readLength);
							byte[] data = new byte[readLength];
							buffer.get(data);
							session.appendData(data);
							session.clearBuffer(buffer);
							session.setNeedNext(false);
						}
						process(session);
					}
				}
			} else {
				if (INFERIOR_COUNT.getAndIncrement() > INFERIOR_COUNT_NUM) {
					session.close();
					return;
				}
				process(session);
			}
		} catch (Throwable throwble) {
			failed(throwble, session);
		}
	}

	@Override
	public void failed(Throwable exc, AbstractSocketSession session) {
		logger.error("", exc);
		if (!session.isClose()) {
			session.close();
		}
	}

	public void process(AbstractSocketSession session) {
		AsynchronousSocketChannel channel = session.getChannel();
		if (!session.isClose()) {
			if (session.isReceived()) {
				channel.read(session.getReadBuffer().getByteBuffer(), session, this);
			} else {
				channel.read(session.getReadBuffer().getByteBuffer(), 5000, TimeUnit.MILLISECONDS, session, this);
			}
		}
	}

}
