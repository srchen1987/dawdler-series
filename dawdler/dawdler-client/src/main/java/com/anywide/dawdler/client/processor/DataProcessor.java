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
package com.anywide.dawdler.client.processor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.client.net.aio.session.SocketSession;
import com.anywide.dawdler.core.bean.AuthResponseBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.core.compression.strategy.CompressionWrapper;
import com.anywide.dawdler.core.compression.strategy.ThresholdCompressionStrategy;
import com.anywide.dawdler.core.handler.IoHandler;
import com.anywide.dawdler.core.handler.IoHandlerFactory;
import com.anywide.dawdler.core.net.buffer.PoolBuffer;
import com.anywide.dawdler.core.serializer.Serializer;
import com.anywide.dawdler.core.thread.InvokeFuture;

/**
 * 
 * @Title: DataProcessor.java
 * @Description: 数据处理者，经过readhandler的粘包后进行包处理。
 * @author: jackson.song
 * @date: 2015年03月12日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class DataProcessor implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(DataProcessor.class);
	private SocketSession socketSession;
	private boolean compress;
	private Serializer serializer;
	private byte[] datas;
	private byte headData;
	private IoHandler ioHandler = IoHandlerFactory.getHandler();

	public DataProcessor(SocketSession socketSession, byte headData, boolean compress, Serializer serializer,
			byte[] datas) {
		this.socketSession = socketSession;
		this.compress = compress;
		this.serializer = serializer;
		this.datas = datas;
		this.headData = headData;
	}

	@Override
	public void run() {
		try {
			process();
		} catch (Exception e) {
			socketSession.close();
			logger.error("", e);
		}

	}

	/*
	 * // in the future will dispatch for user operator static AtomicInteger id =
	 * new AtomicInteger();
	 */
	public void process() throws Exception {
		if (compress)
			datas = ThresholdCompressionStrategy.staticSingle().decompress(datas);
		Object obj = serializer.deserialize(datas);
		if (ioHandler != null)
			ioHandler.messageReceived(socketSession, obj);
		if (obj instanceof ResponseBean) {
			ResponseBean response = (ResponseBean) obj;
			long seq = response.getSeq();
			InvokeFuture<Object> invoke = socketSession.getFutures().remove(seq);
			if (response.getCause() != null)
				invoke.setCause(response.getCause());
			else {
				invoke.setResult(response.getTarget());
			}
		} else if (obj instanceof AuthResponseBean) {
			AuthResponseBean authResponse = (AuthResponseBean) obj;
			if (authResponse.isSuccess()) {
				List<SocketSession> sessions = new ArrayList<>();
				List<SocketSession> preSessions = socketSession.getDawdlerConnection().getSessionGroup()
						.putIfAbsent(socketSession.getRemoteAddress(), sessions);
				if (preSessions != null) {
					sessions = preSessions;
				}
				sessions.add(socketSession);
				if (ioHandler != null)
					ioHandler.channelOpen(socketSession);
				socketSession.getInitLatch().countDown();
				socketSession.getDawdlerConnection().rebuildSessionGroup();
				if (socketSession.getDawdlerConnection().getComplete().compareAndSet(false, true)) {
					socketSession.getDawdlerConnection().getSemaphore().release(Integer.MAX_VALUE);
				}
			} else {
				socketSession.getInitLatch().countDown();
				throw new IllegalAccessException("Invalid auth !");
			}
		} else
			throw new IllegalAccessException("Invalid request!" + obj.getClass().getName());
		datas = null;
	}

	public void write() throws Exception {
		CompressionWrapper cr = ThresholdCompressionStrategy.staticSingle().compress(datas);
		datas = cr.getBuffer();
		synchronized (socketSession) {
			ByteBuffer bf = socketSession.getWriteBuffer();
			int size = datas.length + 1;
			int capacity = size + 4;
			PoolBuffer pb = null;
			try {
				if (capacity > SocketSession.CAPACITY) {
					pb = PoolBuffer.selectPool(capacity);
					if (pb == null) {
						bf = ByteBuffer.allocate(capacity);
						logger.warn("The serialized object is too large.\t size :" + capacity);
					} else
						bf = pb.getByteBuffer();
				}
				bf.putInt(size);
				bf.put((byte) (cr.isCompressed() ? headData | 1 : headData | 0));
				bf.put(datas);
				bf.flip();
				socketSession.write(bf);
			} finally {
				bf.clear();
				if (pb != null) {
					pb.release(bf);
				}
			}
		}
	}
}
