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
package com.anywide.dawdler.server.service.conf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServicesConfig.java
 * @Description 部署服务的配置实体类,代替XmlObject配置类
 * @date 2023年5月1日
 * @email suxuan696@gmail.com
 */
public class ServicesConfig {
	/**
	 * 集成Mybatis后需要配置的mapper
	 */
	private Set<String> mappers;

	/**
	 * 数据源配置
	 */
	private Map<String, Map<String, Object>> dataSources = new HashMap<>();

	/**
	 * 提供远程加载类的配置文件路径
	 */
	private String remoteLoad;

	/**
	 * 预加载类
	 */
	private Set<String> preLoads;

	/**
	 * 扫描路径
	 */
	private Set<String> packagePaths;

	private List<Map<String, String>> dataSourceExpressions;

	private List<Map<String, String>> decisions;

	public Set<String> getMappers() {
		return mappers;
	}

	public void setMappers(Set<String> mappers) {
		this.mappers = mappers;
	}

	public Map<String, Map<String, Object>> getDataSources() {
		return dataSources;
	}

	public String getRemoteLoad() {
		return remoteLoad;
	}

	public void setRemoteLoad(String remoteLoad) {
		this.remoteLoad = remoteLoad;
	}

	public Set<String> getPreLoads() {
		return preLoads;
	}

	public void setPreLoads(Set<String> preLoads) {
		this.preLoads = preLoads;
	}

	public Set<String> getPackagePaths() {
		return packagePaths;
	}

	public void setPackagePaths(Set<String> packagePaths) {
		this.packagePaths = packagePaths;
	}

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

}
