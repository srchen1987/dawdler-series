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
package club.dawdler.core.discoverycenter;

import java.util.List;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * 注册中心接口，支持用这个接口来扩展不同的注册中心
 */
public interface DiscoveryCenter {
	final String ROOT_PATH = "/dawdler";

	/**
	 * 初始化接口
	 */
	void init() throws Exception;

	/**
	 * 销毁接口
	 */
	void destroy() throws Exception;

	/**
	 * 获取服务列表
	 */
	List<String> getServiceList(String path) throws Exception;

	/**
	 * 添加服务提供者
	 */
	boolean addProvider(String path, String value, Map<String, Object> attributes) throws Exception;

	/**
	 * 更新服务提供者
	 */
	default boolean updateProvider(String path, String value, Map<String, Object> attributes) throws Exception {
		return true;
	}

	/**
	 * 删除服务提供者
	 */
	default boolean deleteProvider(String path, String value) throws Exception {
		return true;
	}

	/**
	 * 判断是否存在
	 */
	boolean isExist(String path, String value) throws Exception;
}
