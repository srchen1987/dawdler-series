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
package club.dawdler.client.net.aio.handler;

import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.client.conf.ClientConfig;
import club.dawdler.client.conf.ClientConfigParser;
import club.dawdler.client.net.aio.session.SocketSession;
import club.dawdler.core.bean.AuthRequestBean;
import club.dawdler.core.handler.IoHandler;
import club.dawdler.core.handler.IoHandlerFactory;
import club.dawdler.core.net.aio.handler.ReaderHandler;
import club.dawdler.util.CertificateOperator;

/**
 * @author jackson.song
 * @version V1.0
 * aio实现连接处理器，初始化session，同时做身份校验
 */
public class ConnectorHandler implements CompletionHandler<Void, SocketSession> {
	private static final Logger logger = LoggerFactory.getLogger(ConnectorHandler.class);
	private static final ReaderHandler readerHandler = new ReaderHandler();
	private static final IoHandler ioHandler = IoHandlerFactory.getHandler();

	@Override
	public void completed(Void result, SocketSession session) {
		ClientConfig clientConfig = ClientConfigParser.getClientConfig();
		if (clientConfig == null) {
			return;
		}
		CertificateOperator certificate = new CertificateOperator(clientConfig.getCertificatePath());
		try {
			session.init();
		} catch (Exception e) {
			session.close(false);
			logger.error("", e);
			return;
		}
		if (!session.isClose()) {
			AuthRequestBean auth = new AuthRequestBean();
			try {
				auth.setUser(session.getUser());
				auth.setPassword(certificate.encrypt(session.getPassword().getBytes()));
				auth.setPath(session.getPath());
				session.getDawdlerConnection().writeFirst(session.getPath(), auth, session);
				readerHandler.process(session);
			} catch (Exception e) {
				session.close(false);
				logger.error("", e);
			}
		}
	}

	@Override
	public void failed(Throwable exc, SocketSession socketSession) {
		socketSession.getInitLatch().countDown();
		if (ioHandler != null) {
			ioHandler.exceptionCaught(socketSession, exc);
		}
		try {
			socketSession.close();
		} catch (Exception e) {
			logger.error("", e);
		}
		logger.error("", exc);
	}
}
