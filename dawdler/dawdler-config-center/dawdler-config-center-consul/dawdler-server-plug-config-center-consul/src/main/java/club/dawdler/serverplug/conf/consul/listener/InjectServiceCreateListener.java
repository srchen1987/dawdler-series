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
package club.dawdler.serverplug.conf.consul.listener;

import club.dawdler.conf.Refresher;
import club.dawdler.core.service.listener.DawdlerServiceCreateListener;

/**
 * @author jackson.song
 * @version V1.0
 * 监听服务器端服务创建的事件，注入配置信息
 */
public class InjectServiceCreateListener implements DawdlerServiceCreateListener {

	@Override
	public void create(Object service, boolean single) {
		Refresher.refreshAllConfig(service, single);
	}

}
