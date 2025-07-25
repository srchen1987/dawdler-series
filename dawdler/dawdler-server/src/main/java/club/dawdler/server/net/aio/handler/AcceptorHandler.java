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
package club.dawdler.server.net.aio.handler;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.net.aio.handler.ReaderHandler;
import club.dawdler.server.bootstrap.DawdlerServer;
import club.dawdler.server.conf.ServerConfig.Server;
import club.dawdler.server.context.DawdlerServerContext;
import club.dawdler.server.net.aio.session.SocketSession;

/**
 * @author jackson.song
 * @version V1.0
 * aio接收请求的处理者
 */
public class AcceptorHandler implements CompletionHandler<AsynchronousSocketChannel, DawdlerServerContext> {
	private static final Logger logger = LoggerFactory.getLogger(AcceptorHandler.class);
	private static final ReaderHandler readerHandler = new ReaderHandler();
	private DawdlerServerContext dawdlerServerContext;

	@Override
	public void completed(AsynchronousSocketChannel channel, DawdlerServerContext dawdlerServerContext) {
		this.dawdlerServerContext = dawdlerServerContext;
		AsynchronousServerSocketChannel serverChannel = dawdlerServerContext.getAsynchronousServerSocketChannel();
		config(channel);
		SocketSession socketSession = null;
		try {
			socketSession = new SocketSession(channel);
			socketSession.setDawdlerServerContext(dawdlerServerContext);
			readerHandler.process(socketSession);
		} catch (Exception e) {
			logger.error("", e);
			if (socketSession != null) {
				socketSession.close();
			}
		}
		if (serverChannel.isOpen() && DawdlerServer.isStart()) {
			serverChannel.accept(dawdlerServerContext, this);
		}
	}

	@Override
	public void failed(Throwable exc, DawdlerServerContext dawdlerServerContext) {
		AsynchronousServerSocketChannel serverChannel = dawdlerServerContext.getAsynchronousServerSocketChannel();
		if (serverChannel.isOpen()) {
			serverChannel.accept(dawdlerServerContext, this);
		}

	}

	public void config(AsynchronousSocketChannel channel) {
		Server server = dawdlerServerContext.getServerConfig().getServer();
		try {
			channel.setOption(StandardSocketOptions.TCP_NODELAY, server.isTcpNoDelay());
		} catch (IOException e) {
			logger.error("", e);
		}

		try {
			channel.setOption(StandardSocketOptions.SO_KEEPALIVE, server.isTcpKeepAlive());
		} catch (IOException e) {
			logger.error("", e);
		}
		try {
			channel.setOption(StandardSocketOptions.SO_SNDBUF, server.getTcpSendBuffer());
		} catch (IOException e) {
			logger.error("", e);
		}

		try {
			channel.setOption(StandardSocketOptions.SO_RCVBUF, server.getTcpReceiveBuffer());
		} catch (IOException e) {
			logger.error("", e);
		}

	}
}
