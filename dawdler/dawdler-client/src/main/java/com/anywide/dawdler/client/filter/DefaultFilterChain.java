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
package com.anywide.dawdler.client.filter;
import java.util.concurrent.TimeUnit;

import com.anywide.dawdler.client.net.aio.session.SocketSession;
import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.thread.InvokeFuture;
/**
 * 
 * @Title:  DefaultFilterChain.java
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2015年04月06日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class DefaultFilterChain implements FilterChain {
	@Override
	public Object doFilter(RequestBean request) throws Exception {
		RequestWrapper rq = (RequestWrapper) request;
		SocketSession socketSession = rq.getSession();
		InvokeFuture<?> future = new InvokeFuture<>();
		socketSession.getFutures().put(request.getSeq(),future);
		socketSession.getDawdlerConnection().write(rq.getRequest(), socketSession);
		return future.getResult(rq.getTimeout(), TimeUnit.SECONDS);
	}

}
