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
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author jackson.song
 * @version V1.0
 * @Title PathUtils.java
 * @Description 工具类 获取多个jar包组装成url数组
 * @date 2008年03月28日
 * @email suxuan696@gmail.com
 */
public class PathUtils {
	public static URL[] getLibURL(File file, URL defaultURL) throws MalformedURLException {
		File[] files = file.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		});
		boolean hasDefault = defaultURL != null;
		if (files != null) {
			URL[] urls;
			if (hasDefault) {
				urls = new URL[files.length + 1];
				urls[0] = defaultURL;
				for (int i = 0; i < files.length; i++) {
					urls[i + 1] = files[i].toURI().toURL();
				}
			} else {
				urls = new URL[files.length];
				for (int i = 0; i < files.length; i++) {
					urls[i] = files[i].toURI().toURL();
				}
			}
			return urls;
		} else {
			if (hasDefault) {
				URL[] urls = new URL[1];
				urls[0] = defaultURL;
				return urls;
			}
			return null;
		}
	}
}
