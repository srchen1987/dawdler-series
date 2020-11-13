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
package com.anywide.dawdler.server.deploys;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.server.loader.DawdlerClassLoader;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * 
 * @Title: ServiceRoot.java
 * @Description: deploy下服务模块的根实现
 * @author: jackson.song
 * @date: 2015年03月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ServiceRoot {
	private static Logger logger = LoggerFactory.getLogger(ServiceRoot.class);
	private static Map<String, Service> services = new ConcurrentHashMap<>();
//	private DawdlerServerContext dawdlerServerContext;
	private static final String DAWDLER_DEPLOYS_PATH = "deploys";
	private static final String DAWDLER_LIB_PATH = "lib";
	public static final String DAWDLER_BASE_PATH = "DAWDLER_BASE_PATH";

	public ClassLoader createServerClassLoader() {
		try {
			return DawdlerClassLoader.createLoader(Thread.currentThread().getContextClassLoader(), getLibURL());
		} catch (MalformedURLException e) {
			logger.error("", e);
			return null;
		}
	}

	private String getEnv(String key) {
		return DawdlerTool.getEnv(key);
	}

	private File getDeploys() {
		return new File(getEnv(DAWDLER_BASE_PATH), DAWDLER_DEPLOYS_PATH);
	}

	private URL[] getLibURL() throws MalformedURLException { 
		return PathUtils.getLibURL(new File(getEnv(DAWDLER_BASE_PATH), DAWDLER_LIB_PATH), null);
	}

	public static Service getService(String path) {
		Service sb = services.get(path);
		if (sb == null)
			return null;
		Thread.currentThread().setContextClassLoader(sb.getDawdlerContext().getClassLoader());
		DawdlerContext.setDawdlerContext(sb.getDawdlerContext());
		return sb;
	}

	public void initApplication(DawdlerServerContext dawdlerServerContext) {
//		this.dawdlerServerContext = dawdlerServerContext;
		File file = getDeploys();
		File[] files = file.listFiles();
		long start = JVMTimeProvider.currentTimeMillis();
		if (files.length > 0) {
			ExecutorService es = Executors.newCachedThreadPool();
			ClassLoader classLoader = createServerClassLoader();
			if (classLoader != null) { 
				try {
					DataSourceNamingInit.init(classLoader);
				} catch (ClassNotFoundException | NamingException | InstantiationException | IllegalAccessException e) {
					logger.error("", e);
				}
			}
			Server server =  dawdlerServerContext.getServerConfig().getServer();
			for (File f : files) {
				if (f.isDirectory()) {
					es.execute(() -> {
						String deployName = f.getName();
						try {
							long serviceStart = JVMTimeProvider.currentTimeMillis(); 
							Service service = new ServiceBase(f,server.getHost(),server.getTcpPort(),classLoader);
							services.put(deployName, service);
							service.start(); 
							long serviceEnd = JVMTimeProvider.currentTimeMillis();
							System.out.println(deployName + " startup in " + (serviceEnd - serviceStart) + " ms!");
						} catch (Exception e) {
							Service service = services.remove(deployName);
							service.stop();
							logger.error("", e);
							System.out.println(deployName + " startup failed!");
						}
					});
				}
			}
			es.shutdown();
			try {
				es.awaitTermination(3, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				System.out.println("Server startup time out 3 minutes!");
				return;
			}
			long end = JVMTimeProvider.currentTimeMillis();
			System.out.println("Server startup in " + (end - start) + " ms,Listening port: "+dawdlerServerContext.getServerConfig().getServer().getTcpPort()+"!");
		}
	}
	
	
	public void destroyedApplication() {
		services.values().forEach(v->{
			v.stop();
		});
	}
	
}
