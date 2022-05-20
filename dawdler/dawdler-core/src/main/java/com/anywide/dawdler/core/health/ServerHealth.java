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
package com.anywide.dawdler.core.health;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServerHealth.java
 * @Description ServerHealth 实体类
 * @date 2022年5月3日
 * @email suxuan696@gmail.com
 */
public class ServerHealth {
	private Map<String, Object> data = new LinkedHashMap<String, Object>();

	public void setStatus(String status) {
		data.put("status", status);
	}

	public void setError(String message) {
		data.put("error", message);
	}

	public void addService(ServiceHealth serviceHealth) {
		data.put(serviceHealth.getName(), serviceHealth.getData());
	}

	public Map<String, Object> getData() {
		return data;
	}

	public String getStatus() {
		return (String) data.get("status");
	}

}
