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
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.deploys.loader.DawdlerDeployClassLoader;
import com.anywide.dawdler.util.spring.antpath.StringUtils;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceBase.java
 * @Description deploy下服务模块具体实现类
 * @date 2015年3月22日
 * @email suxuan696@gmail.com
 */
public class ServiceBase extends AbstractService {
	private static final String CLASSES_PATH = "classes";
	private static final String LIB_PATH = "lib";
	private final File deploy;

	public ServiceBase(ServerConfig serverConfig, File deploy, ClassLoader parent, Semaphore startSemaphore,
			AtomicBoolean started) throws Exception {
		super(serverConfig, deploy.getName(), startSemaphore, started);
		URL binPath = serverConfig.getBinPath();
		this.deploy = deploy;
		if (!getClassesDir().isDirectory()) {
			throw new FileNotFoundException(getClassesDir().getAbsolutePath());
		}
		if (!getLibDir().isDirectory()) {
			throw new FileNotFoundException(getLibDir().getAbsolutePath());
		}
		classLoader = new DawdlerDeployClassLoader(dawdlerContext, binPath, parent, getClassLoaderURL(),
				getClassesDir());
		resetContextClassLoader();
		classLoader.loadAspectj();
		dawdlerContext.initServicesConfig();
	}

	private File getClassesDir() {
		return new File(deploy, CLASSES_PATH);
	}

	private File getLibDir() {
		return new File(deploy, LIB_PATH);
	}

	private URL[] getClassLoaderURL() throws MalformedURLException {
		return PathUtils.getLibURL(getLibDir(), getClassesDir().toURI().toURL());
	}

	public class DeployScanner {
		private Set<String> packagePathInJar = new LinkedHashSet<>();
		private Set<String> packageAntPathInJar = new LinkedHashSet<>();
		private Set<String> packagePathInClasses = new LinkedHashSet<>();
		private Set<String> packageAntPathInClasses = new LinkedHashSet<>();

		public void splitAndAddPathInJar(String packagePath) {
			if (!StringUtils.hasLength(packagePath)) {
				return;
			}
			if (antPathMatcher.isPattern(packagePath)) {
				this.packageAntPathInJar.add(packagePath);
			} else {
				this.packagePathInJar.add(packagePath);
			}
		}

		public void splitAndAddPathInClasses(String packagePath) {
			if (!StringUtils.hasLength(packagePath)) {
				return;
			}
			if (antPathMatcher.isPattern(packagePath)) {
				this.packageAntPathInClasses.add(packagePath);
			} else {
				this.packagePathInClasses.add(packagePath);
			}
		}

		public boolean matchInClasses(String packagePath) {
			if (packagePathInClasses.contains(packagePath)) {
				return true;
			}
			for (String antPath : packageAntPathInClasses) {
				if (antPathMatcher.match(antPath, packagePath)) {
					return true;
				}
			}
			return false;
		}

		public boolean matchInJars(String packagePath) {
			if (packagePathInJar.contains(packagePath)) {
				return true;
			}
			for (String antPath : packageAntPathInJar) {
				if (antPathMatcher.match(antPath, packagePath)) {
					return true;
				}
			}
			return false;
		}

	}

}
