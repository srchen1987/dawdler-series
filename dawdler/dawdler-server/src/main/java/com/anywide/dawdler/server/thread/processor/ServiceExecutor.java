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
package com.anywide.dawdler.server.thread.processor;
import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.server.bean.ServicesBean;
/**
 * 
 * @Title:  ServiceExecutor.java
 * @Description:    服务处理器接口   
 * @author: jackson.song    
 * @date:   2015年03月12日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public interface ServiceExecutor {
	public void execute(RequestBean requestBean,ResponseBean responseBean,ServicesBean servicesBean);
}
