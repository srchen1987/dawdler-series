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
package com.anywide.dawdler.serverplug.listener;
import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.ServiceFactory;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateListener;
import com.anywide.dawdler.serverplug.db.dao.DAOFactory;
import com.anywide.dawdler.serverplug.db.dao.SuperDAO;
/**
 * 
 * @Title:  InjectServiceCreateListener.java   
 * @Description:    监听器实现service和dao的注入
 * @author: jackson.song    
 * @date:   2015年07月08日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class InjectServiceCreateListener implements DawdlerServiceCreateListener{
	private static Logger logger = LoggerFactory.getLogger(InjectServiceCreateListener.class);
	@Override
	public void create(Object service,DawdlerContext dawdlerContext) {
		inject(service,dawdlerContext);
	}
	private void inject(Object service,DawdlerContext dawdlerContext) {
		Field[] fields =  service.getClass().getDeclaredFields();
		for(Field filed : fields) {
			Resource resource = filed.getAnnotation(Resource.class);
			RemoteService remoteService = filed.getAnnotation(RemoteService.class);
			if(!filed.getType().isPrimitive()) {
				Class serviceClass = filed.getType();
				filed.setAccessible(true);
				try {
					if(resource!=null&&SuperDAO.class.isAssignableFrom(serviceClass)) {
						filed.set(service, DAOFactory.getInstance().getDAO(serviceClass));
						}
					else if(remoteService!=null) {
						if(remoteService.remote()) {
							String groupName = remoteService.group();
							try {
								filed.set(service, ServiceFactory.getService(serviceClass, groupName));
							} catch (Exception e) { 
								logger.error("",e);
							}
						}
						else
							filed.set(service,dawdlerContext.getServiceProxy(serviceClass));
					}
				} catch (Exception e) {
					logger.error("",e);
				}
			}
	}
}

}
