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

import com.anywide.dawdler.serverplug.bean.XmlBean;
import com.anywide.dawdler.serverplug.load.ReadClass;
import com.anywide.dawdler.serverplug.service.CheckUpdate;
import com.anywide.dawdler.util.XmlObject;

/**
 * 
 * @Title: CheckUpdateImpl.java
 * @Description: 用于获取更新服务器端模版类列表的服务实现
 * @author: jackson.song
 * @date: 2007年09月18日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class CheckUpdateImpl implements CheckUpdate {
	public XmlBean check(String host) {
		XmlObject xmlo = ReadClass.read(host);
		return new XmlBean(xmlo.getDocument());
	}
}
