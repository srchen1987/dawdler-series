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
package com.anywide.dawdler.core.db.health;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import com.anywide.dawdler.core.db.datasource.RWSplittingDataSourceManager;
import com.anywide.dawdler.core.health.Health;
import com.anywide.dawdler.core.health.Health.Builder;
import com.anywide.dawdler.core.health.HealthIndicator;

/**
 * @author jackson.song
 * @version V1.0
 * DataSourceIndicator DataSource健康指示器
 */
public class DataSourceIndicator implements HealthIndicator {

	@Override
	public String name() {
		return "dataSource";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		RWSplittingDataSourceManager manager = RWSplittingDataSourceManager.getInstance();
		Map<String, DataSource> dataSources = manager.getDataSources();
		Set<Map.Entry<String, DataSource>> entrySet = dataSources.entrySet();
		for (Entry<String, DataSource> entry : entrySet) {
			String key = entry.getKey();
			DataSource dataSource = entry.getValue();
			Builder childBuilder = Health.up();
			Connection con = null;
			try {
				con = dataSource.getConnection();
				con.getAutoCommit();
				builder.withDetail(key, childBuilder.build().getData());
			} catch (Exception e) {
				throw e;
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
					}
				}
			}
		}
		return builder.build();
	}

}
