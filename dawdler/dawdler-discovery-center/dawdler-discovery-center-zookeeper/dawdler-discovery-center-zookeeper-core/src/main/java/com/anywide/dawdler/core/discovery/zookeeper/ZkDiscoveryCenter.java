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
package com.anywide.dawdler.core.discovery.zookeeper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import com.anywide.dawdler.core.discoverycenter.DiscoveryCenter;
import com.anywide.dawdler.util.PropertiesUtil;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ZkDiscoveryCenter.java
 * @Description 配置中心zk的实现
 * @date 2018年8月13日
 * @email suxuan696@gmail.com
 */
public class ZkDiscoveryCenter implements DiscoveryCenter {
	private final String ROOT_PATH = "/dawdler";
	private CuratorFramework client;
	private String connectString;
	private String user;
	private String password;
	private AtomicBoolean destroyed = new AtomicBoolean();
	private static ZkDiscoveryCenter zkDiscoveryCenter = null;

	public static synchronized ZkDiscoveryCenter getInstance() throws Exception {
		if (zkDiscoveryCenter == null) {
			zkDiscoveryCenter = new ZkDiscoveryCenter();
		}
		return zkDiscoveryCenter;
	}

	private ZkDiscoveryCenter() throws Exception {
		Properties ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter("zookeeper");
		this.connectString = ps.getProperty("connectString");
		this.user = ps.getProperty("user");
		this.password = ps.getProperty("password");
		if (connectString == null || connectString.trim().equals("")) {
			throw new IllegalArgumentException("zookeeper connectString can not be null or empty!");
		}
		init();
	}

	@Override
	public void init() {
		// 连接时间 和重试次数
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 0);
		if (user != null && !user.trim().equals("") && password != null && !password.trim().equals("")) {
			client = CuratorFrameworkFactory.builder().connectString(connectString)
					.authorization(ROOT_PATH, (user + ":" + password).getBytes()).retryPolicy(retryPolicy).build();
		} else {
			client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
		}
		client.start();
	}

	@Override
	public void destroy() {
		if (destroyed.compareAndSet(false, true)) {
			if (client != null) {
				Field field;
				try {
					field = client.getClass().getDeclaredField("runSafeService");// 这是一个bug,目前已经提交了pr给apache
					field.setAccessible(true);
					ExecutorService runSafeService = (ExecutorService) field.get(client);
					if (runSafeService != null) {
						runSafeService.shutdownNow();
					}
				} catch (Exception e) {
				}
				client.close();
			}

		}
	}

	@Override
	public List<String> getServiceList(String path) throws Exception {
		return client.getChildren().forPath(ROOT_PATH + "/" + path);
	}

	@Override
	public boolean addProvider(String path, String value) throws Exception {
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
				.forPath(ROOT_PATH + "/" + path + "/" + value, value.getBytes());
		return true;
	}

	@Override
	public boolean deleteProvider(String path, String value) throws Exception {
		 client.delete().deletingChildrenIfNeeded().forPath(ROOT_PATH + "/" + path + "/" + value);
		 return true;
	}

	@Override
	public boolean isExist(String path) throws Exception {
		return client.checkExists().forPath(ROOT_PATH + "/" + path) != null;
	}

	public String state() throws Exception {
		return client.getZookeeperClient().getZooKeeper().getState().toString();
	}

	public CuratorFramework getClient() {
		return client;
	}

	public String getConnectString() {
		return connectString;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public AtomicBoolean getDestroyed() {
		return destroyed;
	}

	public String getRootPath() {
		return ROOT_PATH;
	}

}
