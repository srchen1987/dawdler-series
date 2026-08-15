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
package club.dawdler.client.i18n.filter;

import club.dawdler.client.filter.DawdlerClientFilter;
import club.dawdler.client.filter.FilterChain;
import club.dawdler.core.bean.RequestBean;
import club.dawdler.core.rpc.context.RpcContext;
import club.dawdler.util.ResourceBundleUtil;
import club.dawdler.util.TLS;

/**
 * @author jackson.song
 * @version V1.0
 * i18n过滤器用于做rpc请求时放置TLS_LOCALE变量到RpcContext中
 */
public class I18nFilter implements DawdlerClientFilter {
	@Override
	public Object doFilter(RequestBean request, FilterChain chain) throws Exception {
		RpcContext.getContext().setAttachment(ResourceBundleUtil.TLS_LOCALE, TLS.get(ResourceBundleUtil.TLS_LOCALE));
		return chain.doFilter(request);
	}

}
