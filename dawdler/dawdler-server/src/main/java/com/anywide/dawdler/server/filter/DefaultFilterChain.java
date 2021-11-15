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
package com.anywide.dawdler.server.filter;

import java.util.Map;

import com.anywide.dawdler.core.bean.RequestBean;
import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.core.rpc.context.RpcContext;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DefaultFilterChain.java
 * @Description 服务器链的具体实现类
 * @date 2015年4月08日
 * @email suxuan696@gmail.com
 */
public class DefaultFilterChain implements FilterChain {
	@Override
	public void doFilter(RequestBean request, ResponseBean response) {
		RequestWrapper rw = (RequestWrapper) request;
		ServiceExecutor serviceExecutor = rw.getServiceExecutor();
		Map<String, Object> attachments = request.getAttachments();
		try {
			if (attachments != null) {
				RpcContext context = RpcContext.getContext();
				attachments.forEach((k, v) -> {
					context.setAttachment(k, v);
				});
			}
			serviceExecutor.execute(request, response, rw.getServices());
		} finally {
			RpcContext.removeContext();
		}
	}

}
