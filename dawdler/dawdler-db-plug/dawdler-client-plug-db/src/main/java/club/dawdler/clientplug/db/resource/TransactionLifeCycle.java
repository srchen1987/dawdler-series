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
package club.dawdler.clientplug.db.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import club.dawdler.clientplug.web.conf.WebConfig;
import club.dawdler.clientplug.web.conf.WebConfigParser;
import club.dawdler.core.annotation.Order;
import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.db.conf.DataSourceExpression;
import club.dawdler.core.db.conf.DbConfig;
import club.dawdler.core.db.conf.Decision;
import club.dawdler.core.db.datasource.RWSplittingDataSourceManager;

/**
 * @author jackson.song
 * @version V1.0
 * 绑定事务管理器(代替老版本中的PlugInit)
 */
@Order(2)
public class TransactionLifeCycle implements ComponentLifeCycle {

	@Override
	public void prepareInit() throws Throwable {
		WebConfig webConfig = WebConfigParser.getWebConfig();
		DbConfig dbConfig = new DbConfig();
		List<Decision> decisions = new ArrayList<>();
		List<DataSourceExpression> dataSourceExpressions = new ArrayList<>();
		List<Map<String, String>> decisionList = webConfig.getDecisions();
		if (decisionList != null) {
			decisionList.forEach(data -> {
				decisions.add(new Decision(data.get("latentExpressionId"), data.get("mapping")));
			});
		}
		List<Map<String, String>> expressionList = webConfig.getDataSourceExpressions();
		if (expressionList != null) {
			expressionList.forEach(data -> {
				dataSourceExpressions.add(new DataSourceExpression(data.get("id"), data.get("latentExpression")));
			});
		}
		dbConfig.setDecisions(decisions);
		dbConfig.setDataSourceExpressions(dataSourceExpressions);
		RWSplittingDataSourceManager.init(dbConfig);
	}
}
