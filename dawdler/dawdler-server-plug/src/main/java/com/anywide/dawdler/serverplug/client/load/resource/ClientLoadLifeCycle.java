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
package com.anywide.dawdler.serverplug.client.load.resource;

import org.dom4j.Element;

import com.anywide.dawdler.client.conf.ClientConfigParser;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.serverplug.load.LoadCore;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientLoadLifeCycle.java
 * @Description 服务器端加载api,代替PlugInit,PlugInit会被删除掉
 * @date 2022年4月30日
 * @email suxuan696@gmail.com
 */
@Order(1)
public class ClientLoadLifeCycle implements ComponentLifeCycle {

	@Override
	public void prepareInit() throws Throwable {
		Element loadApi = DawdlerContext.getDawdlerContext().getServicesConfig().selectSingleNode("/config/load-api");
		if(loadApi != null) {
			XmlObject xml = ClientConfigParser.getXmlObject();
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

}
