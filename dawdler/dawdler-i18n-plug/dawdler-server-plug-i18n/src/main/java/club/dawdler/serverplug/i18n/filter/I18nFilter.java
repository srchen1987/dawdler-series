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
package club.dawdler.serverplug.i18n.filter;

import java.util.Locale;
import club.dawdler.core.bean.RequestBean;
import club.dawdler.core.bean.ResponseBean;
import club.dawdler.server.filter.DawdlerFilter;
import club.dawdler.server.filter.FilterChain;
import club.dawdler.util.ResourceBundleUtil;
import club.dawdler.util.TLS;

/**
 * @author jackson.song
 * @version V1.0
 * 国际化过滤器 用于在服务端处理请求时设置线程本地存储中的语言环境
 */
public class I18nFilter implements DawdlerFilter {
	@Override
	public void doFilter(RequestBean request, ResponseBean response, FilterChain chain) throws Exception {
		Locale locale = (Locale) request.getAttachment(ResourceBundleUtil.TLS_LOCALE);
		if (locale != null) {
			TLS.set(ResourceBundleUtil.TLS_LOCALE, locale);
		}
		chain.doFilter(request, response);
	}

}
