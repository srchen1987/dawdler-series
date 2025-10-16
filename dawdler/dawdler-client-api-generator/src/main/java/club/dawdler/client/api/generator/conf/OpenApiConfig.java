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
package club.dawdler.client.api.generator.conf;

import club.dawdler.client.api.generator.data.ContactData;
import club.dawdler.client.api.generator.data.ServersData;

/**
 * @author jackson.song
 * @version V1.0
 * openApi配置类
 */
public class OpenApiConfig {
	private String version;
	private String title;
	private String description;
	private ContactData contact;
	private String openApi;
	private ServersData[] servers;
	private String[] scanPath;
	private String outPath;

	public String[] getScanPath() {
		return scanPath;
	}

	public void setScanPath(String[] scanPath) {
		this.scanPath = scanPath;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ContactData getContact() {
		return contact;
	}

	public void setContact(ContactData contact) {
		this.contact = contact;
	}

	public void setServers(ServersData[] servers) {
		this.servers = servers;
	}

	public ServersData[] getServers() {
		return servers;
	}

	public String getOutPath() {
		return outPath;
	}

	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}

	public String getOpenApi() {
		return openApi;
	}

	public void setOpenApi(String openApi) {
		this.openApi = openApi;
	}

}
