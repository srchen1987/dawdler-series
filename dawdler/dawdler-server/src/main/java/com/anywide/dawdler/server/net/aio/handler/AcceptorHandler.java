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
package com.anywide.dawdler.server.net.aio.handler;
import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.core.net.aio.handler.ReaderHandler;
import com.anywide.dawdler.server.bootstarp.DawdlerServer;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.server.net.aio.session.SocketSession;
/**
 * 
 * @Title:  AcceptorHandler.java
 * @Description:    aio接收请求的处理者  
 * @author: jackson.song    
 * @date:   2015年03月12日       
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class AcceptorHandler implements CompletionHandler<AsynchronousSocketChannel,DawdlerServerContext> {
	static Logger logger = LoggerFactory.getLogger(ReaderHandler.class);
	private static ReaderHandler readerHandler = new ReaderHandler();
	private DawdlerServerContext dawdlerServerContext;
	@Override
	public void completed(AsynchronousSocketChannel channel, DawdlerServerContext dawdlerServerContext) {
			this.dawdlerServerContext=dawdlerServerContext;
			AsynchronousServerSocketChannel serverChannel = dawdlerServerContext.getAsynchronousServerSocketChannel();
			config(channel);
			SocketSession socketSession = null;
			try {
				socketSession = new SocketSession(channel);
				socketSession.setDawdlerServerContext(dawdlerServerContext);
				readerHandler.new ReadProcessor(socketSession).run();
			} catch (Exception e) {
				logger.error("",e);
				if(socketSession!=null)
					socketSession.close(false);
			}
			if(serverChannel.isOpen()&&DawdlerServer.isStart()){
				serverChannel.accept(dawdlerServerContext,this);
			} 
		}

	@Override
	public void failed(Throwable exc,DawdlerServerContext dawdlerServerContext) {
		AsynchronousServerSocketChannel serverChannel = dawdlerServerContext.getAsynchronousServerSocketChannel();
		if(serverChannel.isOpen()){
			serverChannel.accept(dawdlerServerContext,this);
		}
		
	}
	public void config(AsynchronousSocketChannel channel){
		Server server = dawdlerServerContext.getServerConfig().getServer();
		try {
			channel.setOption(StandardSocketOptions.TCP_NODELAY, server.isTcpNoDelay());
		} catch (IOException e) {
			logger.error("",e);
		}

		try {
			channel.setOption(StandardSocketOptions.SO_KEEPALIVE, server.isTcpKeepAlive());
		} catch (IOException e) {
			logger.error("",e);
		}
		try {
			channel.setOption(StandardSocketOptions.SO_SNDBUF,server.getTcpSendBuffer());
		} catch (IOException e) {
			logger.error("",e);
		}

		try {
			channel.setOption(StandardSocketOptions.SO_RCVBUF, server.getTcpReceiveBuffer());
		} catch (IOException e) {
			logger.error("",e);
		}

	}
}
