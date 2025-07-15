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

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import club.dawdler.util.DawdlerTool;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

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
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
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
		if (username != null && password != null && username.length() > 0 && password.length() > 0) {
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		}
		RestClient restClient = RestClient.builder(httpHostArray)
				.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
					@Override
					public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
						return requestConfigBuilder.setConnectTimeout(connectTimeout)
								.setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout);
					}
				})
				.setHttpClientConfigCallback(
						httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
								.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLContext(sslContext))
				.build();
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		return new ElasticsearchClient(transport);
	}

	private HttpHost[] getHttpHosts(boolean isSSL) {
		String[] split = hosts.split(",");
		HttpHost[] httpHostArray = new HttpHost[split.length];
		for (int i = 0; i < split.length; i++) {
			String item = split[i];
			httpHostArray[i] = new HttpHost(item.split(":")[0], Integer.parseInt(item.split(":")[1]),
					isSSL ? "https" : "http");
		}
		return httpHostArray;
	}

}
