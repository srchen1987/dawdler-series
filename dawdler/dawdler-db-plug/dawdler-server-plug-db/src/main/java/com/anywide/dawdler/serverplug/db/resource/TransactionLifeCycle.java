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
package com.anywide.dawdler.serverplug.db.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.db.conf.DataSourceExpression;
import com.anywide.dawdler.core.db.conf.DbConfig;
import com.anywide.dawdler.core.db.conf.Decision;
import com.anywide.dawdler.core.db.datasource.RWSplittingDataSourceManager;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.conf.ServicesConfig;

/**
 * @author jackson.song
 * @version V1.0
 * @Title TransactionLifeCycle.java
 * @Description 绑定事务管理器(代替老版本中的PlugInit)
 * @date 2022年4月30日
 * @email suxuan696@gmail.com
 */
@Order(2)
public class TransactionLifeCycle implements ComponentLifeCycle {

	@Override
	public void prepareInit() throws Throwable {
		DawdlerContext dawdlerContext = DawdlerContext.getDawdlerContext();
		ServicesConfig servicesConfig = dawdlerContext.getServicesConfig();
		DbConfig dbConfig = new DbConfig();
		List<Decision> decisions = new ArrayList<>();
		List<DataSourceExpression> dataSourceExpressions = new ArrayList<>();

		List<Map<String, String>> decisionList = servicesConfig.getDecisions();
		if (decisionList != null) {
			decisionList.forEach(data -> {
				decisions.add(new Decision(data.get("latentExpressionId"), data.get("mapping")));
			});
		}
		List<Map<String, String>> expressionList = servicesConfig.getDataSourceExpressions();
		if (expressionList != null) {
			expressionList.forEach(data -> {
				dataSourceExpressions.add(new DataSourceExpression(data.get("id"), data.get("latentExpression")));
			});
		}
		dbConfig.setDecisions(decisions);
		dbConfig.setDataSourceExpressions(dataSourceExpressions);
//		dbConfig.setDataSources(dawdlerContext.getServicesConfig().getDataSources());
		RWSplittingDataSourceManager.init(dbConfig);
	}
}