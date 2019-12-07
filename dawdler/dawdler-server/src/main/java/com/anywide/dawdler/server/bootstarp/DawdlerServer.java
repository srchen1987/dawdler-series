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
package com.anywide.dawdler.server.bootstarp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.core.thread.DataProcessWorkerPool;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.server.net.aio.handler.AcceptorHandler;

/**
 * 
 * @Title: DawdlerServer.java
 * @Description: dawdler服务器
 * @author: jackson.song
 * @date: 2015年04月09日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class DawdlerServer {
	private static AcceptorHandler acceptorInstance = new AcceptorHandler();
	private static Logger logger = LoggerFactory.getLogger(DawdlerServer.class);
	private final AtomicInteger TNUMBER = new AtomicInteger(0);
	private AsynchronousServerSocketChannel serverChannel;
	private AsynchronousChannelGroup asynchronousChannelGroup;
	private DawdlerServerContext dawdlerServerContext;
	private static AtomicBoolean started = new AtomicBoolean();

	public DawdlerServer(ServerConfig serverConfig) throws IOException {
		dawdlerServerContext = new DawdlerServerContext(serverConfig);
		asynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(
				Executors.newFixedThreadPool((Runtime.getRuntime().availableProcessors() * 2 + 1), (r) -> {
					Thread thread = new Thread(r, "dawdler-Server-acceptor#" + (TNUMBER.incrementAndGet()));
					thread.setDaemon(true);
					return thread;
				}));
		Server server = serverConfig.getServer();
		int port = server.getTcpPort();
		int backlog = server.getTcpBacklog();
		serverChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
		serverChannel.bind(new InetSocketAddress(port), backlog);
		dawdlerServerContext.setAsynchronousServerSocketChannel(serverChannel);
	}

	public void await() {
		try {
			asynchronousChannelGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e1) {
			logger.error("", e1);
		}

	}

	private void listenStop() {
		int port = dawdlerServerContext.getServerConfig().getServer().getTcpShutdownPort();
		String shutdownWhiteList = dawdlerServerContext.getServerConfig().getServer().getShutdownWhiteList();
		if (shutdownWhiteList != null && shutdownWhiteList.trim().equals(""))
			return;
		if (shutdownWhiteList == null)
			shutdownWhiteList = "127.0.0.1,localhost";
		String[] shutdownWhiteListArray = shutdownWhiteList.split(",");
		ServerSocket sk = null;
		Socket socket = null;
		boolean closed = false;
		try {
			sk = new ServerSocket(port, 2);
			while (!closed) {
				try {
					socket = sk.accept();
					String hostAddress = socket.getInetAddress().getHostAddress();
					boolean legal = false;
					for (String s : shutdownWhiteListArray) {
						if (legal = s.equals(hostAddress))
							break;
					}
					if (!legal) {
						socket.close();
						continue;
					}
					socket.setSoTimeout(300);
					InputStream input = socket.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(input));
					String command = br.readLine();
					if (command != null) {
						if ("stop".equals(command.trim())) {
							shutdown();
							closed = true;
							return;
						} else if ("stopnow".equals(command.trim())) {
							shutdownNow();
							closed = true;
							return;
						}
					}
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
					}
				}
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			if (closed) {
				try {
					sk.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void start() {
		doAccept();
		if (started.compareAndSet(false, true)) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						shutdownNow();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			});
			new Thread(new Waiter(this)).start();
			new Thread(new Closer()).start();
		}

	}

	public void doAccept() {
		serverChannel.accept(dawdlerServerContext, acceptorInstance);
	}

	public void shutdownNow() throws IOException {
		if (started.compareAndSet(true, false)) {
			ServerConnectionManager.getInstance().closeNow();
			DataProcessWorkerPool.getInstance().shutdownNow();
			if (!asynchronousChannelGroup.isShutdown()) {
				asynchronousChannelGroup.shutdownNow();
			}
			if (serverChannel.isOpen())
				serverChannel.close();
		}
	}

	public void shutdown() throws IOException {
		if (started.compareAndSet(true, false)) {
			ServerConnectionManager sm = ServerConnectionManager.getInstance();
			while (sm.hasTask()) {
				sm.close();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
			sm.closeNow();
			DataProcessWorkerPool.getInstance().shutdown();
			if (!asynchronousChannelGroup.isShutdown()) {
				asynchronousChannelGroup.shutdown();
			}
			if (serverChannel.isOpen())
				serverChannel.close();
		}
	}

	public static boolean isStart() {
		return started.get();
	}

	private class Closer implements Runnable {
		@Override
		public void run() {
			listenStop();
		}

	}

	private class Waiter implements Runnable {
		DawdlerServer ds;

		public Waiter(DawdlerServer ds) {
			this.ds = ds;
		}

		@Override
		public void run() {
			ds.await();
			System.exit(0);
		}

	}
}
