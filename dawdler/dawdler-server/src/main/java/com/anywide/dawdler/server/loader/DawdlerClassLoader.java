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
package com.anywide.dawdler.server.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerClassLoader.java
 * @Description Dawdler类加载器
 * @date 2015年3月09日
 * @email suxuan696@gmail.com
 */
public class DawdlerClassLoader extends URLClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(DawdlerClassLoader.class);
	private URL binPath;
	protected final WeakHashMap<String, URL> urlCache = new WeakHashMap<>();

	public DawdlerClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public DawdlerClassLoader(URL[] urls, ClassLoader parent, URL binPath) {
		super(urls, parent);
		this.binPath = binPath;
	}

	public static DawdlerClassLoader createLoader(URL binPath, ClassLoader parent, URL... urls) {
		DawdlerClassLoader dl = new DawdlerClassLoader(urls, parent, binPath);
		Thread.currentThread().setContextClassLoader(dl);
		return dl;
	}

	@Override
	public URL getResource(String name) {
		if (name == null || name.trim().equals(""))
			name = "/";
		URL url = urlCache.get(name);
		if (url != null)
			return url;
		if (name.equals("/")) {
			urlCache.put(name, binPath);
			return binPath;
		}
		File file = null;
		try {
			file = new File(binPath.toURI().getPath() + name);
		} catch (URISyntaxException e) {
			logger.error("", e);
			return null;
		}
		if (file.exists())
			try {
				url = file.toURI().toURL();
				urlCache.put(name, url);
				return url;
			} catch (MalformedURLException e) {
				logger.error("", e);
				return null;
			}
		return super.getResource(name);
	}

}
