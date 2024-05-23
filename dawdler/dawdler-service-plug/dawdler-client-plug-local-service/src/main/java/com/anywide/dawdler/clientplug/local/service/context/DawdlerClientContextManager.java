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
package com.anywide.dawdler.clientplug.local.service.context;

import com.anywide.dawdler.core.service.ServicesManager;

/**
 * @author jackson.song
 * @version V1.0
 * dawdler web端服务上下文管理器
 */
public class DawdlerClientContextManager {
	private static DawdlerClientContext context;
	static {
		ServicesManager servicesManager = new ServicesManager();
		context = new DawdlerClientContext(servicesManager);
	}

	public static DawdlerClientContext getDawdlerClientContext() {
		return context;
	}

}
