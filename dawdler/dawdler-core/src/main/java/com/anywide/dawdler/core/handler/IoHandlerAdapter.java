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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.net.aio.session.AbstractSocketSession;
import com.anywide.dawdler.core.net.aio.session.SessionIdleType;
public class IoHandlerAdapter implements IoHandler {
	private static Logger logger = LoggerFactory.getLogger(IoHandlerAdapter.class);
	public void channelClose(AbstractSocketSession socketSession) {
		logger.info("session close :{}",socketSession.getRemoteAddress());
	}
	public void channelOpen(AbstractSocketSession socketSession) {
		logger.info("session open :{}",socketSession.getRemoteAddress());
	}
	public void exceptionCaught(AbstractSocketSession socketSession, Throwable caught) {
		logger.error("exceptionCaught socketSession:{}",socketSession,caught);
	}
	public void messageReceived(AbstractSocketSession socketSession, Object msg) {
		logger.debug("messageReceived socketSession:{} msg:{}",socketSession,msg);
	}
	public void channelIdle(AbstractSocketSession socketSession, SessionIdleType idleType) {
		logger.debug("channelIdle socketSession:{} idleType:{}",socketSession,idleType);
	}
	public void messageSent(AbstractSocketSession socketSession, Object msg) {
		logger.debug("messageSent socketSession:{} msg:{}",socketSession,msg);
	}
}