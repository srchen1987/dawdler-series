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
package com.anywide.dawdler.server.net.aio.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.net.aio.session.AbstractSocketSession;
import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.core.thread.DataProcessWorkerPool;
import com.anywide.dawdler.server.bootstarp.ServerConnectionManager;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.server.thread.processor.DataProcessor;

/**
 * @author jackson.song
 * @version V1.0
 * @Title SocketSession.java
 * @Description 服务器端session具体实现类 还有在极端情况下触发的其他粘包规则没有进行System.copyArray优化
 * @date 2015年3月11日
 * @email suxuan696@gmail.com
 */
public class SocketSession extends AbstractSocketSession {
	private static final Logger logger = LoggerFactory.getLogger(SocketSession.class);
	private DawdlerServerContext dawdlerServerContext;
	private byte pathLength;

	public SocketSession(AsynchronousSocketChannel channel) throws IOException {
		super(channel);
	}

	public SocketSession(AsynchronousSocketChannel channel, boolean init) throws IOException {
		super(channel, init);
	}

	public DawdlerServerContext getDawdlerServerContext() {
		return dawdlerServerContext;
	}

	public void setDawdlerServerContext(DawdlerServerContext dawdlerServerContext) {
		this.dawdlerServerContext = dawdlerServerContext;
	}

	public void close() {
		close(true);
	}

	public void close(boolean reconnect) {
		if (close.compareAndSet(false, true)) {
			if (ioHandler != null)
				ioHandler.channelClose(this);
			ServerConnectionManager.getInstance().removeSession(this);
			if (writeBuffer != null) {
				clean(writeBuffer);
				writeBuffer = null;
			}
			if (readBuffer != null) {
				clean(readBuffer);
				readBuffer = null;
			}
			try {
				if (channel != null) {
					channel.close();
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			if (writerIdleTimeout != null) {
				writerIdleTimeout.cancel();
				readerIdleTimeout.cancel();
			}
		}

	}

	public void appendData(byte[] data) {
		if (path == null) {
			if (pathLength > 0) {
				if (position == 0) {
					dataLength = dataLength - pathLength - 2;
					appendData = new byte[dataLength];
				}
				super.appendData(data);
				if (position > pathLength)
					swapPathByte();
				return;
			} else {
				pathLength = data[0];
				dataLength = dataLength - pathLength - 2;
				appendData = new byte[dataLength];
				if (data.length >= pathLength + 1) {
					int i = 0;
					byte[] pathByte = new byte[pathLength];
					for (int j = 1; j < pathLength + 1; j++) {
						pathByte[i] = data[j];
						i++;
					}
					path = new String(pathByte);
					if (data.length >= pathLength + 2) {
						byte[] temp = data;
						data = new byte[data.length - pathLength - 1];
						System.arraycopy(temp, pathLength + 1, data, 0, data.length);
					} else {
						return;
					}
				}
			}
		}
		super.appendData(data);
	}

	private void swapPathByte() {
		byte[] pathByte = new byte[pathLength];
		for (int i = 1; i < pathByte.length + 1; i++) {
			pathByte[i - 1] = appendData[i];
		}
		path = new String(pathByte);
		int size = pathLength + 1;
		position -= size;
		if (position == 0) {
			appendData = new byte[dataLength];
		} else
			System.arraycopy(appendData, pathLength + 1, appendData, 0, appendData.length - pathLength - 1);
	}

	public void messageCompleted() {
		byte[] data = getAppendData();
		DataProcessWorkerPool.getInstance().execute(new DataProcessor(this, headData, compress, serializer, data));
		toPrepare();
	}

	@Override
	public void parseHead(ByteBuffer buffer) {
		byte data = buffer.get();
		headData = data;
		compress = (1 & data) == 1;
		data = (byte) (data >> 1);
		serializer = SerializeDecider.decide(data);
		if (buffer.remaining() > 0) {
			if (path == null) {
				pathLength = buffer.get();
				if (buffer.remaining() > pathLength) {
					byte[] pathByte = new byte[pathLength];
					buffer.get(pathByte);
					path = new String(pathByte);
					dataLength = dataLength - pathLength - 2;
				}
			} else {
				dataLength = dataLength - 1;
			}
			appendData = new byte[dataLength];
		}

	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public void toPrepare() {
		super.toPrepare();
	}

}
