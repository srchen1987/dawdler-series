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
package com.anywide.dawdler.core.httpserver;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.anywide.dawdler.util.CertificateOperator.KeyStoreConfig;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerHttpServer.java
 * @Description DawdlerHttpServer 内置http服务器
 * @date 2022年5月1日
 * @email suxuan696@gmail.com
 */
public class DawdlerHttpServer {
	private Authenticator authenticator;
	private HttpServer httpServer;
	private static final String HTTPS_SCHEME = "https";

	public DawdlerHttpServer(String host, String scheme, int port, int backlog, String username, String password,
			String keyStorePath, String keyPassword) throws Exception {
		InetSocketAddress address = new InetSocketAddress(host, port);
		if (username != null && password != null) {
			authenticator = new Auth("dawdler-realm", username, password);
		}
		if (HTTPS_SCHEME.equals(scheme)) {
			InputStream input = new FileInputStream(keyStorePath);
			try {
				char[] passwordArray = keyPassword.toCharArray();
				KeyStore ks = KeyStore.getInstance(KeyStoreConfig.DKS.name());
				ks.load(input, passwordArray);
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks, passwordArray);
				SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
				sslContext.init(kmf.getKeyManagers(), null, null);
				HttpsServer httpsServer = HttpsServer.create(address, backlog);
				httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
				httpServer = httpsServer;
			} finally {
				if (input != null) {
					input.close();
				}
			}
		} else {
			httpServer = HttpServer.create(address, backlog);
		}

		httpServer.setExecutor(Executors.newSingleThreadExecutor());
	}

	public void addPath(String path, HttpHandler httpHandler) {
		HttpContext context = httpServer.createContext(path, httpHandler);
		if (authenticator != null) {
			context.setAuthenticator(authenticator);
		}
	}

	public void start() {
		httpServer.start();
	}

	public void stop() {
		httpServer.stop(0);
	}

}
