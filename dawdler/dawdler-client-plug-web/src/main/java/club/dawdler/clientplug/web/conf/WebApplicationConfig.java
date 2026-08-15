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
package club.dawdler.clientplug.web.conf;

import java.io.IOException;

import club.dawdler.util.NetworkUtil;

/**
 * @author jackson.song
 * @version V1.0
 * web端配置
 */
public class WebApplicationConfig {
	private String name;
	private String scheme;
	private String host;
	private int port;

	public WebApplicationConfig(String name, String scheme, String host, int port) throws IOException {
		this.name = name;
		this.scheme = scheme;
		this.host = NetworkUtil.getInetAddress(host);
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setNeme(String name) {
		this.name = name;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
