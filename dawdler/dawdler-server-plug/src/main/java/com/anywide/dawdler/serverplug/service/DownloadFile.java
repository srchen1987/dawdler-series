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
package com.anywide.dawdler.serverplug.service;

import com.anywide.dawdler.core.annotation.RemoteService;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DownloadFile.java
 * @Description 下载模版类文件的接口
 * @date 2007年9月18日
 * @email suxuan696@gmail.com
 */
@RemoteService
public interface DownloadFile {
	Object download(String[] names);
}
