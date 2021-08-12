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
package com.anywide.dawdler.server.listener;

import com.anywide.dawdler.server.context.DawdlerContext;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerServiceListener.java
 * @Description 服务器启动监听器 服务器端的类实现此接口即可
 * @date 2015年4月19日
 * @email suxuan696@gmail.com
 */
public interface DawdlerServiceListener {
	
	void contextInitialized(DawdlerContext dawdlerContext) throws Exception;

	void contextDestroyed(DawdlerContext dawdlerContext) throws Exception;

	
}
