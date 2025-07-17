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
package club.dawdler.client.processor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import club.dawdler.client.net.aio.session.SocketSession;
import club.dawdler.core.bean.AuthResponseBean;
import club.dawdler.core.bean.ResponseBean;
import club.dawdler.core.compression.strategy.ThresholdCompressionStrategy;
import club.dawdler.core.handler.IoHandler;
import club.dawdler.core.handler.IoHandlerFactory;
import club.dawdler.core.serializer.Serializer;
import club.dawdler.core.thread.InvokeFuture;

/**
 * @author jackson.song
 * @version V1.0
 * 数据处理者，经过readhandler的粘包后进行包处理。
 */
public class DataProcessor {
	private static final IoHandler ioHandler = IoHandlerFactory.getHandler();

	public static void process(SocketSession socketSession, boolean compress, Serializer serializer, byte[] data)
			throws Exception {
		if (compress) {
			data = ThresholdCompressionStrategy.staticSingle().decompress(data);
		}
		Object obj = serializer.deserialize(data);
		if (ioHandler != null) {
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
					List<SocketSession> sessions = new CopyOnWriteArrayList<>();
					List<SocketSession> preSessions = socketSession.getDawdlerConnection().getSessionGroup()
							.putIfAbsent(socketSession.getRemoteAddress(), sessions);
					if (preSessions != null) {
						sessions = preSessions;
					}
					sessions.add(socketSession);
					if (ioHandler != null) {
						ioHandler.channelOpen(socketSession);
					}
					socketSession.getDawdlerConnection().rebuildSessionGroup();
					socketSession.getInitLatch().countDown();
					if (socketSession.getDawdlerConnection().getComplete().compareAndSet(false, true)) {
						socketSession.getDawdlerConnection().getSemaphore().release(Integer.MAX_VALUE);
					}
				} else {
					socketSession.getInitLatch().countDown();
					if (socketSession.getDawdlerConnection().getComplete().compareAndSet(false, true)) {
						socketSession.getDawdlerConnection().getSemaphore().release(Integer.MAX_VALUE);
					}
					throw new IllegalAccessException("Invalid auth !");
				}
			} else {
				throw new IllegalAccessException("Invalid request!" + obj.getClass().getName());
			}
		}
	}
}
