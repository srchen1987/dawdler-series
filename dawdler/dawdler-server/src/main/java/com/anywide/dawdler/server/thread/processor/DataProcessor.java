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
package com.anywide.dawdler.server.thread.processor;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.bean.AuthRequestBean;
import com.anywide.dawdler.core.bean.AuthResponseBean;
import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.core.compression.strategy.CompressionWrapper;
import com.anywide.dawdler.core.compression.strategy.ThresholdCompressionStrategy;
import com.anywide.dawdler.core.exception.AuthFailedException;
import com.anywide.dawdler.core.handler.IoHandler;
import com.anywide.dawdler.core.handler.IoHandlerFactory;
import com.anywide.dawdler.core.net.buffer.PoolBuffer;
import com.anywide.dawdler.core.serializer.Serializer;
import com.anywide.dawdler.core.thread.InvokeFuture;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.bootstarp.ServerConnectionManager;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.Service;
import com.anywide.dawdler.server.deploys.ServiceRoot;
import com.anywide.dawdler.server.filter.RequestWrapper;
import com.anywide.dawdler.server.net.aio.session.SocketSession;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DataProcessor.java
 * @Description 服务器端经过readHandler读取粘包数据的处理类
 * @date 2015年03月12日
 * @email suxuan696@gmail.com
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
		if (compress)
			data = ThresholdCompressionStrategy.staticSingle().decompress(data);
		Object obj = serializer.deserialize(data);
		if (ioHandler != null)
			ioHandler.messageReceived(socketSession, obj);
		if (obj instanceof RequestBean) {
			if (!socketSession.isAuthored())
				throw new IllegalAccessException("unauthorized access ！");
			RequestBean requestBean = (RequestBean) obj;
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
				DawdlerContext.remove();
				socketSession.getFutures().remove(requestBean.getSeq());
			}
			data = serializer.serialize(responseBean);
			write();
		} else if (obj instanceof AuthRequestBean) {
			AuthRequestBean authRequest = (AuthRequestBean) obj;
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
				if (ioHandler != null)
					ioHandler.channelOpen(socketSession);
				ServerConnectionManager.getInstance().addSession(socketSession);
			} else
				throw new AuthFailedException(socketSession.getRemoteAddress() + " auth failed!");
			data = serializer.serialize(authResponse);
			write();
		} else
			throw new IllegalAccessException("Invalid request!" + obj.getClass().getName());
		data = null;
	}

	public void write() throws Exception {
		CompressionWrapper cr = ThresholdCompressionStrategy.staticSingle().compress(data);
		data = cr.getBuffer();
		synchronized (socketSession) {
			ByteBuffer bf = socketSession.getWriteBuffer();
			int size = data.length + 1;
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
				bf.put((byte) (cr.isCompressed() ? headData | 1 : headData));
				bf.put(data);
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
