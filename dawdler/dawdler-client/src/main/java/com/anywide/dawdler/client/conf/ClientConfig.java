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
package com.anywide.dawdler.client.conf;

import java.util.ArrayList;
import java.util.List;

import com.anywide.dawdler.util.DawdlerTool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientConfig.java
 * @Description 配置类
 * @date 2015年3月16日
 * @email suxuan696@gmail.com
 */
public class ClientConfig {

	public String certificatePath;

	private List<ServerChannelGroup> serverChannelGroups;

	public ClientConfig() {
		serverChannelGroups = new ArrayList<>();
	}

	public List<ServerChannelGroup> getServerChannelGroups() {
		return serverChannelGroups;
	}

	public String getCertificatePath() {
		if (certificatePath != null) {
			certificatePath = certificatePath.replace("${CLASSPATH}", DawdlerTool.getCurrentPath());
		}
		return certificatePath;
	}

	public void setCertificatePath(String certificatePath) {
		this.certificatePath = certificatePath;
	}

	public class ServerChannelGroup {
		private String groupId;

		private int connectionNum;

		private int sessionNum;

		private int serializer;

		private String user;

		private String password;

		private String host;

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getGroupId() {
			return groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}

		public int getConnectionNum() {
			return connectionNum;
		}

		public void setConnectionNum(int connectionNum) {
			this.connectionNum = connectionNum;
		}

		public int getSerializer() {
			return serializer;
		}

		public void setSerializer(int serializer) {
			this.serializer = serializer;
		}

		public int getSessionNum() {
			return sessionNum;
		}

		public void setSessionNum(int sessionNum) {
			this.sessionNum = sessionNum;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		@Override
		public String toString() {
			return "gid:" + groupId + (host == null ? "" : host) + " connectionNum:" + connectionNum + " sessionNum:"
					+ sessionNum + " serializer:" + serializer;
		}

	}

}
