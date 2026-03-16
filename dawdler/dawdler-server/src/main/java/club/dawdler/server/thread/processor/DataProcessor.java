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
package club.dawdler.server.thread.processor;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.bean.AuthRequestBean;
import club.dawdler.core.bean.AuthResponseBean;
import club.dawdler.core.bean.RequestBean;
import club.dawdler.core.bean.ResponseBean;
import club.dawdler.core.compression.strategy.CompressionWrapper;
import club.dawdler.core.compression.strategy.ThresholdCompressionStrategy;
import club.dawdler.core.handler.IoHandler;
import club.dawdler.core.handler.IoHandlerFactory;
import club.dawdler.core.net.buffer.DawdlerByteBuffer;
import club.dawdler.core.net.buffer.PoolBuffer;
import club.dawdler.core.serializer.Serializer;
import club.dawdler.core.service.bean.ServicesBean;
import club.dawdler.core.service.processor.ServiceExecutor;
import club.dawdler.core.thread.InvokeFuture;
import club.dawdler.server.bootstrap.ServerConnectionManager;
import club.dawdler.server.conf.ServerConfig;
import club.dawdler.server.deploys.Service;
import club.dawdler.server.deploys.ServiceRoot;
import club.dawdler.server.filter.RequestWrapper;
import club.dawdler.server.net.aio.session.SocketSession;

/**
 * @author jackson.song
 * @version V1.0
 * 服务器端经过readHandler读取粘包数据的处理类
 */
public class DataProcessor implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(DataProcessor.class);
	private final SocketSession socketSession;
	private final boolean compress;
	private final Serializer serializer;
	private final byte headData;
	protected IoHandler ioHandler = IoHandlerFactory.getHandler();
	private byte[] data;

	public DataProcessor(SocketSession socketSession, byte headData, boolean compress, Serializer serializer,
			byte[] data) {
		this.socketSession = socketSession;
		this.compress = compress;
		this.serializer = serializer;
		this.data = data;
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

	public void process() throws Exception {
		String path = socketSession.getPath();
		Service service = ServiceRoot.getService(path);
		if (compress) {
			data = ThresholdCompressionStrategy.staticSingle().decompress(data);
		}
		Object obj = serializer.deserialize(data);
		if (ioHandler != null) {
			ioHandler.messageReceived(socketSession, obj);
		}
		if (obj instanceof RequestBean requestBean) {
			if (!socketSession.isAuthored()) {
				throw new IllegalAccessException("unauthorized access !");
			}
			String serviceName = requestBean.getServiceName();
			ServicesBean servicesBean = null;
			if (service != null) {
				servicesBean = service.getServicesBean(serviceName);
			}
			ResponseBean responseBean = new ResponseBean();
			responseBean.setSeq(requestBean.getSeq());
			InvokeFuture<Object> invoke = new InvokeFuture<>();
			socketSession.getFutures().put(requestBean.getSeq(), invoke);
			try {
				if (servicesBean != null) {
					ServiceExecutor serviceExecutor = service.getServiceExecutor();
					RequestWrapper requestWrapper = new RequestWrapper(requestBean, servicesBean, serviceExecutor,
							socketSession);
					service.getFilterProvider().doFilter(requestWrapper, responseBean);
				} else {
					responseBean.setCause(new ClassNotFoundException(serviceName + " in path :( " + path + " )"));
				}
			} finally {
				socketSession.getFutures().remove(requestBean.getSeq());
			}
			data = serializer.serialize(responseBean);
			write();
		} else if (obj instanceof AuthRequestBean authRequest) {
			AuthResponseBean authResponse = new AuthResponseBean();
			ServerConfig serverConfig = socketSession.getDawdlerServerContext().getServerConfig();
			boolean success = false;
			try {
				success = serverConfig.auth(authRequest.getPath(), authRequest.getUser(), authRequest.getPassword());
			} catch (Exception e) {
				logger.error("", e);
			}
			if (success) {
				authResponse.setSuccess(true);
				socketSession.setAuthored(true);
				if (ioHandler != null) {
					ioHandler.channelOpen(socketSession);
				}
				ServerConnectionManager.getInstance().addSession(socketSession);
			} else {
				logger.warn(socketSession.getRemoteAddress() + " auth failed!");
			}
			data = serializer.serialize(authResponse);
			write();
		} else {
			throw new IllegalAccessException("Invalid request!" + obj.getClass().getName());
		}
		data = null;
	}

	public void write() throws Exception {
		CompressionWrapper compressionWrapper = ThresholdCompressionStrategy.staticSingle().compress(data);
		data = compressionWrapper.getBuffer();
		synchronized (socketSession) {
			if (socketSession.isClose()) {
				return;
			}
			DawdlerByteBuffer dawdlerByteBuffer = socketSession.getWriteBuffer();
			ByteBuffer buffer = dawdlerByteBuffer.getByteBuffer();
			int size = data.length + 1;
			int capacity = size + 4;
			PoolBuffer pool = null;
			try {
				if (capacity > SocketSession.CAPACITY) {
					pool = PoolBuffer.selectPool(capacity);
					if (pool == null) {
						buffer = ByteBuffer.allocate(capacity);
						logger.warn("The serialized object is too large.\t size :" + capacity);
					} else {
						dawdlerByteBuffer = pool.getByteBuffer();
						buffer = dawdlerByteBuffer.getByteBuffer();
					}
				}
				buffer.putInt(size);
				buffer.put((byte) (compressionWrapper.isCompressed() ? headData | 1 : headData));
				buffer.put(data);
				buffer.flip();
				socketSession.write(buffer);
			} finally {
				buffer.clear();
				if (pool != null) {
					pool.release(dawdlerByteBuffer);
				}
			}
		}
	}
}
