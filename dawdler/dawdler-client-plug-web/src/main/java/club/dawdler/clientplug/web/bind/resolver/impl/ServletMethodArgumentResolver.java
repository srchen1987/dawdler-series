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
package club.dawdler.clientplug.web.bind.resolver.impl;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import club.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import club.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;
import club.dawdler.clientplug.web.handler.ViewForward;

/**
 * @author jackson.song
 * @version V1.0
 * 获取servlet或基于servlet api相关参数值的决策者
 */
public class ServletMethodArgumentResolver implements MethodArgumentResolver {

	@Override
	public boolean isSupport(RequestParamFieldData requestParamFieldData) {
		Class<?> type = requestParamFieldData.getType();
		return (HttpServletRequest.class == type || HttpServletResponse.class == type || HttpSession.class == type
				|| InputStream.class == type || Reader.class == type || PrintWriter.class == type
				|| Locale.class == type || Locale.class == type || ViewForward.class == type);
	}

	@Override
	public Object resolveArgument(RequestParamFieldData requestParamFieldData, ViewForward viewForward, String uri)
			throws Exception {
		Class<?> type = requestParamFieldData.getType();
		if (ViewForward.class == type) {
			return viewForward;
		} else if (HttpServletRequest.class == type) {
			return viewForward.getRequest();
		} else if (HttpServletResponse.class == type) {
			return viewForward.getResponse();
		} else if (HttpSession.class == type) {
			return viewForward.getRequest().getSession();
		} else if (InputStream.class == type) {
			return viewForward.getRequest().getInputStream();
		} else if (Reader.class == type) {
			return viewForward.getRequest().getReader();
		} else if (PrintWriter.class == type) {
			return viewForward.getResponse().getWriter();
		} else if (Locale.class == type) {
			return viewForward.getRequest().getLocale();
		} else {
			return null;
		}

	}

}
