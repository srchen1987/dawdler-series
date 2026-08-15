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
package club.dawdler.es.restclient.factory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

import club.dawdler.util.DawdlerTool;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;

/**
 * @author jackson.song
 * @version V1.0
 * EsClientFactory工厂
 */
public class EsClientFactory {
	private String username;
	private String password;
	private String hosts;
	private int connectionRequestTimeout;// connection pool中获得一个connection的超时时间
	private int connectTimeout;// 链接建立的超时时间
	private int socketTimeout;// 响应超时时间
	private String keystorePath;// 证书路径
	private String keystorePassword;// 证书密码

	public EsClientFactory(String username, String password, String hosts, int connectionRequestTimeout,
			int connectTimeout, int socketTimeout, String keystorePath, String keystorePassword) {
		this.username = username;
		this.password = password;
		this.hosts = hosts;
		this.connectionRequestTimeout = connectionRequestTimeout;
		this.connectTimeout = connectTimeout;
		this.socketTimeout = socketTimeout;
		this.keystorePath = keystorePath;
		this.keystorePassword = keystorePassword;
	}

	public ElasticsearchClient create() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
			CertificateException, IOException {
		final SSLContext sslContext;
		boolean isSSL = false;
		if (keystorePassword != null && keystorePassword.length() > 0 && keystorePath != null
				&& keystorePath.length() > 0) {
			sslContext = SSLContexts.custom()
					.loadTrustMaterial(DawdlerTool.getResourceURLFromClassPath(keystorePath),
							keystorePassword.toCharArray(),
							new TrustSelfSignedStrategy())
					.build();
			isSSL = true;
		} else {
			sslContext = null;
		}
		HttpHost[] httpHostArray = getHttpHosts(isSSL);
		Rest5ClientBuilder builder = Rest5Client.builder(httpHostArray);
		if (username != null && password != null && username.length() > 0 && password.length() > 0) {
			String cred = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
			builder.setDefaultHeaders(new Header[] { new BasicHeader("Authorization", "Basic " + cred) });
		}
		if (sslContext != null) {
			builder.setConnectionManagerCallback(connectionManagerBuilder -> connectionManagerBuilder.setTlsStrategy(
					new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE)));
		}
		builder.setConnectionConfigCallback(connectConf -> {
			if (connectTimeout > 0) {
				connectConf.setConnectTimeout(Timeout.ofMilliseconds(connectTimeout));
			}
			if (socketTimeout > 0) {
				connectConf.setSocketTimeout(Timeout.ofMilliseconds(socketTimeout));
			}
		});
		builder.setRequestConfigCallback(requestConf -> {
			if (connectionRequestTimeout > 0) {
				requestConf.setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionRequestTimeout));
			}
		});
		Rest5Client restClient = builder.build();
		ElasticsearchTransport transport = new Rest5ClientTransport(restClient, new JacksonJsonpMapper());
		return new ElasticsearchClient(transport);
	}

	private HttpHost[] getHttpHosts(boolean isSSL) {
		String[] split = hosts.split(",");
		HttpHost[] httpHostArray = new HttpHost[split.length];
		for (int i = 0; i < split.length; i++) {
			String item = split[i];
			httpHostArray[i] = new HttpHost(isSSL ? "https" : "http", item.split(":")[0],
					Integer.parseInt(item.split(":")[1]));
		}
		return httpHostArray;
	}

}
