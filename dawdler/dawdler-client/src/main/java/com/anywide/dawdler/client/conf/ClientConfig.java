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

import java.util.List;

import com.anywide.dawdler.util.DawdlerTool;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientConfig.java
 * @Description xstream映射的一个配置类
 * @date 2015年03月16日
 * @email suxuan696@gmail.com
 */
@XStreamAlias("conf")
public class ClientConfig {
	@XStreamAlias("certificatePath")
	public String certificatePath;
	@XStreamAlias("zk-host")
	private String zkHost;
	@XStreamImplicit(itemFieldName = "server-channel-group")
	private List<ServerChannelGroup> serverChannelGroups;

	public String getZkHost() {
		return zkHost;
	}

	public void setZkHost(String zkHost) {
		this.zkHost = zkHost;
	}

	public List<ServerChannelGroup> getServerChannelGroups() {
		return serverChannelGroups;
	}

	public void setServerChannelGroups(List<ServerChannelGroup> serverChannelGroups) {
		this.serverChannelGroups = serverChannelGroups;
	}

	public String getCertificatePath() {
		if (certificatePath != null)
			certificatePath = certificatePath.replace("${CLASSPATH}", DawdlerTool.getcurrentPath());
		return certificatePath;
	}

	public void setCertificatePath(String certificatePath) {
		this.certificatePath = certificatePath;
	}

	public class ServerChannelGroup {
		@XStreamAlias("channel-group-id")
		@XStreamAsAttribute
		private String groupId;

		@XStreamAlias("service-path")
		@XStreamAsAttribute
		private String path;

		@XStreamAlias("connection-num")
		@XStreamAsAttribute
		private int connectionNum;

		@XStreamAlias("session-num")
		@XStreamAsAttribute
		private int sessionNum;

		@XStreamAlias("serializer")
		@XStreamAsAttribute
		private int serializer;

		@XStreamAlias("user")
		@XStreamAsAttribute
		private String user;

		@XStreamAlias("password")
		@XStreamAsAttribute
		private String password;

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

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
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

		@Override
		public String toString() {
			return "gid:" + groupId + " connectionNum:" + connectionNum + " sessionNum:" + sessionNum + " serializer:"
					+ serializer;
		}

	}

}
