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
package club.dawdler.clientplug.i18n.interceptor;

import java.util.Locale;

import club.dawdler.clientplug.web.annotation.RequestMapping;
import club.dawdler.clientplug.web.handler.ViewForward;
import club.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import club.dawdler.util.ResourceBundleUtil;
import club.dawdler.util.TLS;

/**
 * @author jackson.song
 * @version V1.0
 * 国际化拦截器 用于在Web请求处理前设置当前线程的语言环境，确保在请求处理过程中能够正确获取对应语言的国际化资源。
 */
public class I18nInterceptor implements HandlerInterceptor {

	/**
	 * 在请求处理前设置当前线程的语言环境
	 * 
	 * @param controller     控制器实例
	 * @param viewForward    视图转发对象
	 * @param requestMapping 请求映射注解
	 * @return true表示继续处理请求，false表示中断请求处理
	 * @throws Exception 处理异常
	 */
	@Override
	public boolean preHandle(Object controller, ViewForward viewForward, RequestMapping requestMapping)
			throws Exception {
		Locale locale = viewForward.getRequest().getLocale();
		String lang = viewForward.paramString("locale");
		if (lang != null && !lang.isEmpty()) {
			String[] parts = lang.split("_");
			if (parts.length == 1) {
				locale = new Locale(parts[0]);
			} else if (parts.length == 2) {
				locale = new Locale(parts[0], parts[1]);
			} else if (parts.length >= 3) {
				locale = new Locale(parts[0], parts[1], parts[2]);
			}
		}
		TLS.set(ResourceBundleUtil.TLS_LOCALE, locale);
		return true;
	}

	/**
	 * 在请求处理后执行
	 * 
	 * @param controller     控制器实例
	 * @param viewForward    视图转发对象
	 * @param requestMapping 请求映射注解
	 * @param ex             异常信息
	 * @throws Exception 处理异常
	 */
	@Override
	public void postHandle(Object controller, ViewForward viewForward, RequestMapping requestMapping, Throwable ex)
			throws Exception {
	}

	/**
	 * 在请求完成后执行
	 * 
	 * @param controller     控制器实例
	 * @param viewForward    视图转发对象
	 * @param requestMapping 请求映射注解
	 * @param ex             异常信息
	 */
	@Override
	public void afterCompletion(Object controller, ViewForward viewForward, RequestMapping requestMapping,
			Throwable ex) {
	}

}
