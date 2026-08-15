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
package club.dawdler.server.net.aio.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.net.aio.session.AbstractSocketSession;
import club.dawdler.serializer.SerializeDecider;
import club.dawdler.server.bootstrap.ServerConnectionManager;
import club.dawdler.server.context.DawdlerServerContext;
import club.dawdler.server.thread.processor.DataProcessor;

/**
 * @author jackson.song
 * @version V1.0
 * 服务器端session具体实现类 还有在极端情况下触发的其他粘包规则没有进行System.copyArray优化
 */
public class SocketSession extends AbstractSocketSession {
	private static final Logger logger = LoggerFactory.getLogger(SocketSession.class);
	private DawdlerServerContext dawdlerServerContext;
	private byte pathLength;

	public SocketSession(AsynchronousSocketChannel channel) throws Exception {
		super(channel, true);
	}

	public DawdlerServerContext getDawdlerServerContext() {
		return dawdlerServerContext;
	}

	public void setDawdlerServerContext(DawdlerServerContext dawdlerServerContext) {
		this.dawdlerServerContext = dawdlerServerContext;
	}

	public void close() {
		synchronized (writeLock) {
			if (!close.compareAndSet(false, true)) {
				return;
			}
			if (ioHandler != null) {
				try {
					ioHandler.channelClose(this);
				} catch (Throwable e) {
					logger.error("ioHandler.channelClose throw exception, session:{}", describe, e);
				}
			}
			ServerConnectionManager.getInstance().removeSession(this);
			if (writeBuffer != null) {
				clean(writeBuffer);
				writeBuffer = null;
			}
			if (readBuffer != null) {
				clean(readBuffer);
				readBuffer = null;
			}

			if (channel != null) {
				try {
					channel.shutdownInput();
				} catch (IOException e) {
					logger.error("", e);
				}
				try {
					channel.shutdownOutput();
				} catch (IOException e) {
					logger.error("", e);
				}
				try {
					if (channel.isOpen()) {
						channel.close();
					}
				} catch (IOException e) {
					logger.error("", e);
				}
			}
			if (writerIdleTimeout != null) {
				writerIdleTimeout.cancel();
			}
			if (readerIdleTimeout != null) {
				readerIdleTimeout.cancel();
			}
		}
	}

	public void appendData(byte[] data) {
		if (path == null) {
			if (pathLength > 0) {
				if (position == 0) {
					dataLength = dataLength - pathLength - 1;
					if (dataLength < 0) {
						throw new IllegalArgumentException("invalid dataLength: " + dataLength + ", pathLength: "
								+ pathLength + ", from: " + getRemoteAddress());
					}
					appendData = new byte[dataLength];
				}
				super.appendData(data);
				if (position > pathLength) {
					swapPathByte();
				}
				return;
			} else {
				pathLength = data[0];
				if (pathLength <= 0) {
					throw new IllegalArgumentException("invalid pathLength: " + pathLength + ", dataLength: "
							+ dataLength + ", from: " + getRemoteAddress());
				}
				dataLength = dataLength - pathLength - 1;
				if (dataLength < 0) {
					throw new IllegalArgumentException("invalid dataLength: " + dataLength + ", pathLength: "
							+ pathLength + ", from: " + getRemoteAddress());
				}
				appendData = new byte[dataLength];
				if (data.length >= pathLength + 1) {
					byte[] pathByte = new byte[pathLength];
					System.arraycopy(data, 1, pathByte, 0, pathLength);
					path = new String(pathByte, StandardCharsets.UTF_8);
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
		System.arraycopy(appendData, 1, pathByte, 0, pathLength);
		path = new String(pathByte, StandardCharsets.UTF_8);
		int size = pathLength + 1;
		position -= size;
		if (position == 0) {
			appendData = new byte[dataLength];
		} else {
			System.arraycopy(appendData, pathLength + 1, appendData, 0, appendData.length - pathLength - 1);
		}
	}

	public void messageCompleted() {
		byte[] data = getAppendData();
		dawdlerServerContext.execute(new DataProcessor(this, headData, compress, serializer, data));
		toPrepare();
	}

	@Override
	public void parseHead(ByteBuffer buffer) {
		byte data = buffer.get();
		headData = data;
		compress = (1 & data) == 1;
		data = (byte) (data >> 1);
		serializer = SerializeDecider.decide(data);
		dataLength = dataLength - 1;
		if (buffer.remaining() > 0 && path == null) {
			pathLength = buffer.get();
			if (pathLength <= 0) {
				throw new IllegalArgumentException("invalid pathLength: " + pathLength + ", dataLength: "
						+ dataLength + ", from: " + getRemoteAddress());
			}
			if (buffer.remaining() > pathLength) {
				byte[] pathByte = new byte[pathLength];
				buffer.get(pathByte);
				path = new String(pathByte, StandardCharsets.UTF_8);
				dataLength = dataLength - pathLength - 1;
			}
		}
		if (dataLength < 0) {
			throw new IllegalArgumentException("invalid dataLength: " + dataLength + ", pathLength: "
					+ pathLength + ", path: " + path + ", from: " + getRemoteAddress());
		}
		appendData = new byte[dataLength];
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public void toPrepare() {
		super.toPrepare();
	}

}
