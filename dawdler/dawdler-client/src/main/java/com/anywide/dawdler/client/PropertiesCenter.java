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
package com.anywide.dawdler.client;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.client.conf.ClientConfig;
import com.anywide.dawdler.client.conf.ClientConfigParser;
/**
 * 
 * @Title:  PropertiesCenter.java
 * @Description:    配置中心，目前采用zookeeper   
 * @author: jackson.song    
 * @date:  	2015年03月18日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class PropertiesCenter {
	private static Logger logger = LoggerFactory.getLogger(PropertiesCenter.class);
	private static PropertiesCenter instance = new PropertiesCenter();
	private static final String ROOTPATH="/dawdler";
	private TreeCache treeCache = null;
	private static CuratorFramework client;
	private PropertiesCenter() {
		ClientConfig clientConfig = ClientConfigParser.getClientConfig();
		String connectString = clientConfig.getZkHost();
	        // 连接时间 和重试次数
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,0);
        client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client.start();
        try {
			initListenter();
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	public static PropertiesCenter getInstance(){
		return instance;
	}
	public String getValue(String path) throws Exception{
		return new String(client.getData().forPath(ROOTPATH+"/"+path));
	}
	private void initListenter() throws Exception{
			treeCache = new TreeCache(client,ROOTPATH);
	        treeCache.getListenable().addListener(new TreeCacheListener() {
	            @Override
	            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
	                ChildData data = event.getData();
	                if(data !=null){
	                    switch (event.getType()) {
	                    case NODE_ADDED:{
	                    	if(data.getData() != null)
	                    		logger.info(data.getPath()+" add "+new String(data.getData()));
	                        break;
	                    }
	                    case NODE_REMOVED:
	                     	logger.info(" remove "+data.getPath());
	                        break;
	                    case NODE_UPDATED:{
	                        String gid = data.getPath().replace(ROOTPATH+"/","");
	                        ConnectionPool cp = ConnectionPool.getConnectionPool(gid);
	                        if(cp!=null){
	                        	cp.doChange("update",new String(data.getData()),gid);
	                        }
	                        break;
	                    }
	                    default:
	                        break;
	                    }
	                }
	            }
	        });
	        //开始监听
	        treeCache.start();
	    } 
	public void close() {
		treeCache.close();
		client.close();
	}

}
