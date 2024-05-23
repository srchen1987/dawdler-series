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
package com.anywide.dawdler.serverplug.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.serverplug.load.ReadClass;
import com.anywide.dawdler.serverplug.load.bean.RemoteFiles;
import com.anywide.dawdler.serverplug.service.DownloadFile;

/**
 * @author jackson.song
 * @version V1.0
 * 下载模版类文件的接口的具体实现类
 */
public class DownloadFileBean implements DownloadFile {
	private static final Logger logger = LoggerFactory.getLogger(DownloadFileBean.class);

	public RemoteFiles download(String[] names) {
		try {
			return ReadClass.readRemoteFiles(names);
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}
}
