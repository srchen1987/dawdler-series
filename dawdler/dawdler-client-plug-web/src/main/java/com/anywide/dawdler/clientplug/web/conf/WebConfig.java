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
package com.anywide.dawdler.clientplug.web.conf;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jackson.song
 * @version V1.0
 * web配置
 */
public class WebConfig {

	private List<Map<String, String>> dataSourceExpressions;

	private List<Map<String, String>> decisions;

	private Set<String> mappers;

	/**
	 * 扫描路径
	 */
	private Set<String> packagePaths;

	public List<Map<String, String>> getDataSourceExpressions() {
		return dataSourceExpressions;
	}

	public void setDataSourceExpressions(List<Map<String, String>> dataSourceExpressions) {
		this.dataSourceExpressions = dataSourceExpressions;
	}

	public List<Map<String, String>> getDecisions() {
		return decisions;
	}

	public void setDecisions(List<Map<String, String>> decisions) {
		this.decisions = decisions;
	}

	public Set<String> getMappers() {
		return mappers;
	}

	public void setMappers(Set<String> mappers) {
		this.mappers = mappers;
	}

	public Set<String> getPackagePaths() {
		return packagePaths;
	}

	public void setPackagePaths(Set<String> packagePaths) {
		this.packagePaths = packagePaths;
	}

}
