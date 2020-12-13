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

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.core.net.aio.session.AbstractSocketSession;

/**
 * 
 * @Title: ReaderHandler.java
 * @Description: aio读包的处理者，相对比较复杂 实现粘包等功能
 * @author: jackson.song
 * @date: 2015年03月12日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ReaderHandler implements CompletionHandler<Integer, AbstractSocketSession> {
	static Logger logger = LoggerFactory.getLogger(ReaderHandler.class);
	private static final int HEADERFIELDLENGTH = Integer.BYTES;
	private final AtomicInteger INFERIORCOUNT = new AtomicInteger();
	private int INFERIORCOUNT_NUM = 10;

	@Override
	public void completed(Integer result, AbstractSocketSession session) {
		if (result == -1) {
			logger.warn(session + "\tsession close.");
			session.close();
			return;
		}
		session.setLastReadTime(System.currentTimeMillis());
		if (result > 0) {
			ByteBuffer buffer = session.getReadBuffer();
			if (session.isReceived()) {
				if (session.isNeedNext() ? buffer.remaining() <= HEADERFIELDLENGTH
						: buffer.position() <= HEADERFIELDLENGTH) {
					if (session.isNeedNext()) {
						byte[] temp = new byte[buffer.remaining()];
						buffer.get(temp);
						buffer.clear();
						buffer.put(temp);
						session.setNeedNext(false);
					}
					process(session);
					return;
				}
				session.toConnectionState();
				if (!session.isNeedNext())
					buffer.flip();
				int dataLength = buffer.getInt();
				if (dataLength == 0) {
					if (buffer.remaining() > 0) {
						session.toReceiveState();
						session.setNeedNext(true);
						completed(buffer.remaining(), session);
					} else {
						try {
							session.clearBuffer(buffer);
							session.toPrepare();
							process(session);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					return;
				}
				session.setDataLength(dataLength);
				session.setPackageSize(dataLength);
				int readLength = buffer.remaining();
				if (readLength > dataLength) {
					session.parseHead(buffer);
					session.appendReadLenth(dataLength);
					buffer.get(session.getAppendData());
					session.messageCmpleted();
					session.setNeedNext(true);
					completed(buffer.remaining(), session);
					return;
				} else if (buffer.remaining() == dataLength) {
					session.parseHead(buffer);
					session.appendReadLenth(dataLength);
					buffer.get(session.getAppendData());
					session.clearBuffer(buffer);
					session.messageCmpleted();
					process(session);
				} else {
					session.appendReadLenth(readLength);
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
					session.appendReadLenth(remanentDataLength);
					session.appendData(data);
					session.messageCmpleted();
					session.setNeedNext(true);
					completed(buffer.remaining(), session);
					return;
				} else {
					if (readLength == remanentDataLength) {
						session.appendReadLenth(remanentDataLength);
						byte[] data = new byte[remanentDataLength];
						buffer.get(data);
						session.appendData(data);
						session.clearBuffer(buffer);
						session.messageCmpleted();
					} else {
						session.appendReadLenth(readLength);
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
			if (INFERIORCOUNT.getAndIncrement() > INFERIORCOUNT_NUM) {
				session.close();
				return;
			}
			process(session);
		}
	}

	@Override
	public void failed(Throwable exc, AbstractSocketSession session) {
		logger.warn("fialed", exc);
		session.close();
	}

	public void process(AbstractSocketSession session) {
		AsynchronousSocketChannel channel = session.getChannel();
		if (!session.isClose()) {
			if (session.isReceived())
				channel.read(session.getReadBuffer(), session, this);
			else
				channel.read(session.getReadBuffer(), 5000, TimeUnit.MILLISECONDS, session, this);
		}
	}

	public class ReadProcessor implements Runnable {
		private AbstractSocketSession socketSession;

		public ReadProcessor(AbstractSocketSession socketSession) {
			this.socketSession = socketSession;
		}

		@Override
		public void run() {
			try {
				process(socketSession);
			} catch (Exception e) {
				socketSession.close();
				logger.error("", e);
			}
		}

	}
}
