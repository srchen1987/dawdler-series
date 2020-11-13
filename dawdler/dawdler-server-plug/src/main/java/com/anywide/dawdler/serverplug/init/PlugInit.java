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
import java.io.IOException;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.ServiceBase;
import com.anywide.dawdler.serverplug.datasource.RWSplittingDataSourceManager;
import com.anywide.dawdler.serverplug.load.ClientConfig;
import com.anywide.dawdler.serverplug.load.LoadCore;
import com.anywide.dawdler.serverplug.transaction.TransactionServiceExecutor;
import com.anywide.dawdler.util.XmlObject;
/**
 * 
 * @Title:  PlugInit.java   
 * @Description:    服务器端插件，主要是做调用远端的注入service与模版类   
 * @author: jackson.song    
 * @date:   2015年07月12日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class PlugInit{
	private static Logger logger = LoggerFactory.getLogger(PlugInit.class);
	public PlugInit(DawdlerContext dawdlerContext) {
		RWSplittingDataSourceManager dm;
		try {
			dm = new RWSplittingDataSourceManager();
			dawdlerContext.setAttribute(RWSplittingDataSourceManager.DATASOURCEMANAGER_PREFIX,dm);
			dawdlerContext.setAttribute(ServiceBase.SERVICEEXECUTOR_PREFIX,new TransactionServiceExecutor());
			XmlObject xml = ClientConfig.getInstance().getXml();
			if(xml!=null) {
				for(Object o:xml.getNode("/config/loads-on/item")){
					Element ele = (Element) o;
					String host = ele.getText();
//					if(LoadListener.DEBUG)System.out.println("starting load.....\t"+host+"\tmodule!");
					String channelGroupId = ele.attributeValue("channel-group-id");
					String time = ele.attributeValue("time");
					int checkTime = 0;
					try {
						checkTime=Integer.parseInt(time);
					} catch (Exception e) {
					}
					LoadCore loadCore = new LoadCore(host,checkTime,channelGroupId);
					try {
						loadCore.toCheck();
					} catch (IOException e) {
						logger.error("",e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("",e);
		}
		
		
	}
	

}
