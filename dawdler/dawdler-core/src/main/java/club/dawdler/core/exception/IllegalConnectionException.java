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
package club.dawdler.core.exception;

/**
 * @author jackson.song
 * @version V1.0
 * 不合规连接异常
 */
public class IllegalConnectionException extends RuntimeException {

	private static final long serialVersionUID = -2638144867099257710L;

	public IllegalConnectionException(String error, String ipAddress, int dataSize) {
		super(error);
		this.ipAddress = ipAddress;
		this.dataSize = dataSize;
	}

	private String ipAddress;

	private int dataSize;

	public String getIpAddress() {
		return ipAddress;
	}

	public int getDataSize() {
		return dataSize;
	}

}
