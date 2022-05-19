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
package com.anywide.dawdler.core.discoverycenter;

import java.util.List;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DiscoveryCenter.java
 * @Description 代替 PropertiesCenter.java(已删除) 当年写的着急，先用zk实现
 *              不考虑扩展其他的，目前支持用这个接口来扩展
 * @date 2018年8月13日
 * @email suxuan696@gmail.com
 */
public interface DiscoveryCenter {

	List<String> getServiceList(String path) throws Exception;

	void init() throws Exception;

	void destroy() throws Exception;

	boolean addProvider(String path, String value) throws Exception;

	boolean updateProvider(String path, String value) throws Exception;

	boolean deleteProvider(String path, String value) throws Exception;

	boolean isExist(String path) throws Exception;
}
