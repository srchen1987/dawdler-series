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
package com.anywide.dawdler.serverplug.db.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.ServiceBase;
import com.anywide.dawdler.serverplug.db.datasource.RWSplittingDataSourceManager;
import com.anywide.dawdler.serverplug.db.transaction.TransactionServiceExecutor;

/**
 * @author jackson.song
 * @version V1.0
 * @Title PlugInit.java
 * @Description 服务器端插件，绑定事务管理器（将com.anywide.dawdler.serverplug.init.PlugInit
 *              拆分出来）
 * @date 2021年03月12日
 * @email suxuan696@gmail.com
 */
public class PlugInit {
	private static final Logger logger = LoggerFactory.getLogger(PlugInit.class);

	public PlugInit(DawdlerContext dawdlerContext) {
		RWSplittingDataSourceManager dm;
		try {
			dm = new RWSplittingDataSourceManager();
			dawdlerContext.setAttribute(RWSplittingDataSourceManager.DATASOURCE_MANAGER_PREFIX, dm);
			dawdlerContext.setAttribute(ServiceBase.SERVICE_EXECUTOR_PREFIX, new TransactionServiceExecutor());
		} catch (Exception e) {
			logger.error("", e);
		}

	}

}