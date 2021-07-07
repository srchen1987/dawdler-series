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
package com.anywide.dawdler.core.handler;

import com.anywide.dawdler.core.net.aio.session.AbstractSocketSession;
import com.anywide.dawdler.core.net.aio.session.SessionIdleType;

/**
 * @author jackson.song
 * @version V1.0
 * @Title IoHandler.java
 * @Description 网络IO事件 客户端，服务器端接收，发送，开关连接，空闲，异常事件的接口
 * @date 2015年03月12日
 * @email suxuan696@gmail.com
 */
public interface IoHandler {
	void messageReceived(AbstractSocketSession socketSession, Object msg);

	void channelOpen(AbstractSocketSession socketSession);

	void channelClose(AbstractSocketSession socketSession);

	void exceptionCaught(AbstractSocketSession socketSession, Throwable caught);

	void channelIdle(AbstractSocketSession socketSession, SessionIdleType idleType);

	void messageSent(AbstractSocketSession socketSession, Object response);
}