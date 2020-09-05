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
package com.anywide.dawdler.clientplug.web.session.message;
/**
 * 
 * @Title:  MessageOperator.java
 * @Description:  消息操作者
 * @author: jackson.song    
 * @date:   2016年6月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public interface MessageOperator {
	
	public void sendMessageToSet(String sessionkey, String attributeName, Object attributeValue);
	
	public void sendMessageToDel(String sessionkey, String attributeName);
	
	public void listenExpireAndDelAndChange();
	
}
