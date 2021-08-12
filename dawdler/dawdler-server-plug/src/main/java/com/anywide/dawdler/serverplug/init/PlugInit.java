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
package com.anywide.dawdler.serverplug.init;

import org.dom4j.Element;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.serverplug.load.ClientConfig;
import com.anywide.dawdler.serverplug.load.LoadCore;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title PlugInit.java
 * @Description 服务器端插件，主要是做调用远端的注入service与模版类
 * @date 2015年7月12日
 * @email suxuan696@gmail.com
 */
public class PlugInit {

	public PlugInit(DawdlerContext dawdlerContext) throws Exception {
		XmlObject xml = ClientConfig.getInstance().getXml();
		if (xml != null) {
			for (Object o : xml.selectNodes("/config/loads-on/item")) {
				Element ele = (Element) o;
				String host = ele.getText();
				String channelGroupId = ele.attributeValue("channel-group-id");
				String time = ele.attributeValue("time");
				int checkTime = 0;
				try {
					checkTime = Integer.parseInt(time);
				} catch (Exception e) {
				}
				LoadCore loadCore = new LoadCore(host, checkTime, channelGroupId);
				loadCore.toCheck();
			}
		}

	}

}