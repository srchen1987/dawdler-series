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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfigParser;
import com.anywide.dawdler.util.DawdlerTool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title Bootstrap.java
 * @Description dawdler服务器启动类
 * @date 2015年4月9日
 * @email suxuan696@gmail.com
 */
public class Bootstrap {
	public final static Logger logger = LoggerFactory.getLogger("system.out");
	public static final String DAWDLER_BASE_PATH = "DAWDLER_BASE_PATH";
	private static final String DAWDLER_BIN_PATH = "bin";

	private static ServerConfig serverConfig;

	private static void initServerConfig() throws Exception {
		ServerConfigParser serverConfigParser = new ServerConfigParser(getBinURL());
		serverConfig = serverConfigParser.getServerConfig();
	}

	public static void main(String[] args) throws Exception {
		initServerConfig();
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
			printServerInfo();
			new DawdlerServer(serverConfig).start();
		}
	}

	private static void toClose(String comment) throws UnknownHostException, IOException {
		Socket socket = null;
		PrintWriter pw = null;
		try {
			socket = new Socket("127.0.0.1", serverConfig.getServer().getTcpShutdownPort());
			OutputStream out = socket.getOutputStream();
			pw = new PrintWriter(new OutputStreamWriter(out));
			pw.println(comment);
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (socket != null) {
				socket.close();
			}
		}
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

	private static void printServerInfo() {
		System.out.println("Welcome to use dawdler!\n");
		String logoAscii = "  _____              __          __  _____    _        ______   _____  \r\n"
				+ " |  __ \\      /\\     \\ \\        / / |  __ \\  | |      |  ____| |  __ \\ \r\n"
				+ " | |  | |    /  \\     \\ \\  /\\  / /  | |  | | | |      | |__    | |__) |\r\n"
				+ " | |  | |   / /\\ \\     \\ \\/  \\/ /   | |  | | | |      |  __|   |  _  / \r\n"
				+ " | |__| |  / ____ \\     \\  /\\  /    | |__| | | |____  | |____  | | \\ \\ \r\n"
				+ " |_____/  /_/    \\_\\     \\/  \\/     |_____/  |______| |______| |_|  \\_\\\r\n"
				+ "                                                                       \r\n"
				+ "                                                                       ";
		System.out.println(logoAscii);
		DawdlerTool.printServerBaseInformation();
	}

	private static URL getBinURL() throws MalformedURLException {
		return new File(DawdlerTool.getProperty(DAWDLER_BASE_PATH), DAWDLER_BIN_PATH).toURI().toURL();
	}

}
