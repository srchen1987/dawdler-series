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
package com.anywide.dawdler.clientplug.web.handler;

import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.web.bind.RequestMethodProcessor;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.interceptor.InterceptorProvider;
import com.anywide.dawdler.clientplug.web.plugs.AbstractDisplayPlug;
import com.anywide.dawdler.clientplug.web.plugs.DisplaySwitcher;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.util.ClassUtil;
import com.anywide.dawdler.util.JsonProcessUtil;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * @author jackson.song
 * @version V1.0
 * @Title AbstractUrlHandler.java
 * @Description urlHendler父类
 * @date 2007年4月18日
 * @email suxuan696@gmail.com
 */
public abstract class AbstractUrlHandler {
	private final List<OrderData<HandlerInterceptor>> handlerInterceptors = InterceptorProvider
			.getHandlerInterceptors();

	public boolean preHandle(Object target, ViewForward viewForward, RequestMapping requestMapping) throws Exception {
		if (handlerInterceptors != null)
			for (OrderData<HandlerInterceptor> handlerInterceptor : handlerInterceptors) {
				if (!handlerInterceptor.getData().preHandle(target, viewForward, requestMapping))
					return false;
			}
		return true;
	}

	public void postHandle(Object target, ViewForward viewForward, RequestMapping requestMapping, Throwable ex)
			throws Exception {
		if (handlerInterceptors != null) {
			for (int i = handlerInterceptors.size(); i > 0; i--) {
				handlerInterceptors.get(i - 1).getData().postHandle(target, viewForward, requestMapping, ex);
			}
		}
	}

	public void afterCompletion(Object target, ViewForward viewForward, RequestMapping requestMapping, Throwable ex) {
		if (handlerInterceptors != null) {
			for (int i = handlerInterceptors.size(); i > 0; i--) {
				handlerInterceptors.get(i - 1).getData().afterCompletion(target, viewForward, requestMapping, ex);
			}
		}
	}

	public abstract boolean handleUrl(String urishort, String method, HttpServletRequest request,
			HttpServletResponse response) throws ServletException;

	protected boolean invokeMethod(Object target, Method method, RequestMapping requestMapping, ViewForward viewForward,
			boolean responseBody) throws Throwable {
		try {
			if (!preHandle(target, viewForward, requestMapping)) {
				return true;
			}
			Object[] args = RequestMethodProcessor.process(target, viewForward, method);
			Object result = method.invoke(target, args);
			if (responseBody && result != null) {
				HttpServletResponse response = viewForward.getResponse();
				PrintWriter out = response.getWriter();
				try {
					if (result.getClass() == String.class || ClassUtil.isSimpleValueType(result.getClass())) {
						response.setContentType(AbstractDisplayPlug.MIME_TYPE_TEXT_HTML);
						out.print(result);
						out.flush();
					} else {
						response.setContentType(AbstractDisplayPlug.MIME_TYPE_JSON);
						JsonProcessUtil.beanToJson(out, result);
						out.flush();
					}
					postHandle(target, viewForward, requestMapping, viewForward.getInvokeException());
				} finally {
					out.close();
				}
				return true;
			}
			postHandle(target, viewForward, requestMapping, viewForward.getInvokeException());
			DisplaySwitcher.switchDisplay(viewForward);
		} finally {
			afterCompletion(target, viewForward, requestMapping, viewForward.getInvokeException());
		}
		return true;
	}

	protected ViewForward createViewForward() {
		return ViewControllerContext.getViewForward();
	}

}
