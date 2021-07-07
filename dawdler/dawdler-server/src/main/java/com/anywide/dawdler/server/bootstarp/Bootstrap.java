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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfigParser;

/**
 * @author jackson.song
 * @version V1.0
 * @Title Bootstrap.java
 * @Description dawdler服务器启动类
 * @date 2015年04月09日
 * @email suxuan696@gmail.com
 */
public class Bootstrap {
	public final static Logger logger = LoggerFactory.getLogger("system.out");
	private static final ServerConfig serverConfig = ServerConfigParser.getServerConfig();

	public static void main(String[] args) throws IOException {
		if (args != null && args.length > 0) {
			String command = args[0].trim();
			switch (command) {
			case "start":
				overrideOutPut();
				break;
			case "run":
				break;
			case "stop":
			case "stopnow":
				toClose(command);
				return;
			default:
				System.err.println("warn: failed command " + command);
				return;
			}
		}

		new DawdlerServer(serverConfig).start();
	}

	private static void toClose(String comment) throws IOException {
		Socket socket = new Socket("127.0.0.1", serverConfig.getServer().getTcpShutdownPort());
		OutputStream out = socket.getOutputStream();
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
		pw.println(comment);
		pw.close();
		socket.close();
	}

	public static void overrideOutPut() {
		OutputStream output = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				logger.info(String.valueOf(b));
			}

			@Override
			public void write(byte[] b) throws IOException {
				logger.info(new String(b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				logger.info(new String(b, off, len));
			}
		};
		System.setOut(new PrintStream(output));
		System.setErr(new PrintStream(output));
	}
}
