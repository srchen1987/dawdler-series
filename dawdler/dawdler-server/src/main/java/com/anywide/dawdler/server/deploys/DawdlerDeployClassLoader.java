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
import java.util.WeakHashMap;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.loader.DawdlerClassLoader;

/**
 * 
 * @Title: DawdlerDeployClassLoader.java
 * @Description: Dawdler部署在deploys下的类加载器
 * @author: jackson.song
 * @date: 2015年03月09日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class DawdlerDeployClassLoader extends DawdlerClassLoader {
	private DawdlerContext dawdlerContext;
	private WeakHashMap<String, URL> urlcache = new WeakHashMap<>();

	public DawdlerDeployClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	void setDawdlerContext(DawdlerContext dawdlerContext) {
		this.dawdlerContext = dawdlerContext;
	}

	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}

	public static DawdlerDeployClassLoader createLoader(ClassLoader parent, URL... urls) {
		return new DawdlerDeployClassLoader(urls, parent);
	}

	@Override
	public URL getResource(String name) {
		if (name == null || name.trim().equals(""))
			name = "/";
		URL url = urlcache.get(name);
		if (url != null)
			return url;
		if (name.equals("/")) {
			try {
				url = new File(dawdlerContext.getDeployClassPath()).toURI().toURL();
				urlcache.put(name, url);
				return url;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}
		File file = new File(dawdlerContext.getDeployClassPath() + name);
		if (file.exists())
			try {
				url = file.toURI().toURL();
				urlcache.put(name, url);
				return url;
			} catch (MalformedURLException e) {
				return null;
			}
		return super.getResource(name);
	}

	@Override
	public String toString() {
		return getClass().getName() + "\tfor service : " + dawdlerContext.getDeployName();
	}
}
