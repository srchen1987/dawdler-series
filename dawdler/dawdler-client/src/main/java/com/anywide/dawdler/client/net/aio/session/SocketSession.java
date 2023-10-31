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
package com.anywide.dawdler.client.net.aio.session;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.DawdlerConnection;
import com.anywide.dawdler.client.processor.DataProcessor;
import com.anywide.dawdler.core.exception.SessionCloseException;
import com.anywide.dawdler.core.net.aio.session.AbstractSocketSession;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.thread.InvokeFuture;

/**
 * @author jackson.song
 * @version V1.0
 * @Title SocketSession.java
 * @Description 客户端session 主要功能在父类中，包含了 读写超时重连、心跳处理等方式。
 * @date 2015年3月12日
 * @email suxuan696@gmail.com
 */
public class SocketSession extends AbstractSocketSession {
	private static final Logger logger = LoggerFactory.getLogger(SocketSession.class);
	private String user;
	private String password;
	private DawdlerConnection dawdlerConnection;
	private ClassLoader classLoader;

	public SocketSession(AsynchronousSocketChannel channel) throws Exception {
		super(channel, false);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public DawdlerConnection getDawdlerConnection() {
		return dawdlerConnection;
	}

	public void setDawdlerConnection(DawdlerConnection dawdlerConnection) {
		this.dawdlerConnection = dawdlerConnection;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void close() {
		close(true);
	}

	public synchronized void close(boolean reconnect) {
		if (close.compareAndSet(false, true)) {
			if (ioHandler != null) {
				ioHandler.channelClose(this);
			}
			Collection<InvokeFuture<Object>> invokeFuture = getFutures().values();
				invokeFuture.forEach(
						(future) -> future.setCause(new SessionCloseException("session closed. " + this.toString())));
			Map<SocketAddress, List<SocketSession>> sessionGroup = dawdlerConnection.getSessionGroup();
			if (remoteAddress != null) {
				List<SocketSession> sessions = sessionGroup.remove(remoteAddress);
				if (sessions != null) {
					dawdlerConnection.rebuildSessionGroup();
					sessions.clear();
				}
			}
			if (writeBuffer != null) {
				clean(writeBuffer);
				writeBuffer = null;
			}
			if (readBuffer != null) {
				clean(readBuffer);
				readBuffer = null;
			}
			try {
				if (reconnect && dawdlerConnection != null) {
					dawdlerConnection.getConnectManager().addDisconnectAddress(remoteAddress);
				}
				if (channel != null) {
					channel.close();
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			if (writerIdleTimeout != null) {
				writerIdleTimeout.cancel();
			}
			if (readerIdleTimeout != null) {
				readerIdleTimeout.cancel();
			}
		}

	}

	public void messageCompleted() {
		byte[] data = getAppendData();
		try {
			DataProcessor.process(this, compress, serializer, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally {
			if (markClose.get() && futures.isEmpty()) {
				close(false);
			}
			data = null;
			toPrepare();
		}
	}

	@Override
	public void parseHead(ByteBuffer buffer) {
		byte data = buffer.get();
		headData = data;
		compress = (1 & data) == 1;
		data = (byte) (data >> 1);
		serializer = SerializeDecider.decide(data);
		dataLength--;
		appendData = new byte[dataLength];
	}

}
