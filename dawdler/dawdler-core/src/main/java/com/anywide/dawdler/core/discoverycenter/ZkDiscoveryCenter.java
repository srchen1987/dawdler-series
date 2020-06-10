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

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * 
 * @Title:ZkDiscoveryCenter.java 
 * @Description: 配置中心zk的实现 代替 PropertiesCenter.java
 * @author: jackson.song
 * @date: 2018年08月13日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ZkDiscoveryCenter implements DiscoveryCenter {
	protected static final String ROOTPATH = "/dawdler";
	protected TreeCache treeCache = null;
	protected CuratorFramework client;
	protected String connectString;
	protected String user;
	protected String password;
	public ZkDiscoveryCenter(String connectString,String user,String password) {
		this.connectString = connectString;
		this.user = user;
		this.password = password;
		init();
	}

	


	@Override
	public void init(){
		// 连接时间 和重试次数
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 0);
		client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
		client.start();
		
	}

	@Override
	public void destroy(){
		if(treeCache!=null)
				treeCache.close();
		client.close();
	}

	@Override
	public List<String> getServiceList(String path) throws Exception {
		return client.getChildren().forPath(ROOTPATH + "/"+path);
	}




	@Override
	public boolean addProvider(String path,String value) throws Exception {
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).
		forPath(ROOTPATH + "/"+path,value.getBytes());
		return true;
	}




	@Override
	public boolean updateProvider(String path, String value) throws Exception {
		client.setData().forPath(path, value.getBytes());
		return true;
	}
}
