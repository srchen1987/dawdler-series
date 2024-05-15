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

import com.anywide.dawdler.core.service.annotation.Service;
import com.anywide.dawdler.serverplug.bean.XmlBean;

/**
 * @author jackson.song
 * @version V1.0
 * @Title CheckUpdate.java
 * @Description 用于获取更新服务器端模版类列表的服务
 * @date 2007年9月18日
 * @email suxuan696@gmail.com
 */
@Service
public interface CheckUpdate {
	XmlBean check(String host);
}
