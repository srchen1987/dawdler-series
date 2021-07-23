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

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.bind.RequestMethodProcessor;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.interceptor.InterceptorProvider;
import com.anywide.dawdler.clientplug.web.plugs.AbstractDisplayPlug;
import com.anywide.dawdler.clientplug.web.plugs.DisplaySwitcher;
import com.anywide.dawdler.clientplug.web.util.JsonProcessUtil;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.util.ClassUtil;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AbstractUrlHandler.java
 * @Description urlHendler父类
 * @date 2007年04月18日
 * @email suxuan696@gmail.com
 */
public abstract class AbstractUrlHandler {
	private final List<OrderData<HandlerInterceptor>> handlerInterceptors = InterceptorProvider
			.getHandlerInterceptors();

	public boolean preHandle(TransactionController tc) throws Exception {
		if (handlerInterceptors != null)
			for (OrderData<HandlerInterceptor> handlerInterceptor : handlerInterceptors) {
				if (!handlerInterceptor.getData().preHandle(tc))
					return false;
			}
		return true;
	}

	public void postHandle(TransactionController tc, Throwable ex) throws Exception {
		if (handlerInterceptors != null) {
			for (int i = handlerInterceptors.size(); i > 0; i--) {
				handlerInterceptors.get(i - 1).getData().postHandle(tc, ex);
			}
		}
	}

	public void afterCompletion(TransactionController tc, Throwable ex) {
		if (handlerInterceptors != null) {
			for (int i = handlerInterceptors.size(); i > 0; i--) {
				handlerInterceptors.get(i - 1).getData().afterCompletion(tc, ex);
			}
		}
	}

	public abstract boolean handleUrl(String urishort, String method, boolean isJson, HttpServletRequest request,
			HttpServletResponse response) throws ServletException;

	protected boolean invokeMethod(TransactionController targetobj, Method method, ViewForward wf, boolean responseBody)
			throws Throwable {
		try {
			if (!preHandle(targetobj))
				return true;

			Object result = method.invoke(targetobj, RequestMethodProcessor.process(targetobj, wf, method));
			if (responseBody && result != null) {
				HttpServletResponse response = targetobj.getResponse();
				PrintWriter out = response.getWriter();
				try {
					if (ClassUtil.isSimpleValueType(result.getClass())) {
						response.setContentType(AbstractDisplayPlug.MIME_TYPE_TEXT_HTML);
						out.print(result);
						out.flush();
					} else {
						response.setContentType(AbstractDisplayPlug.MIME_TYPE_JSON);
						JsonProcessUtil.beanToJson(out, result);
						out.flush();
					}
				} finally {
					out.close();
				}
				return true;
			}
			postHandle(targetobj, wf.getInvokeException());
		} catch (Throwable e) {
			wf.setInvokeException(e);
		}
		try {
			if (wf.getInvokeException() == null) {
				DisplaySwitcher.switchDisplay(wf);
			} else {
				throw wf.getInvokeException();
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			afterCompletion(targetobj, wf.getInvokeException());
		}
		return true;
	}

	protected ViewForward createViewForward() {
		return ViewControllerContext.getViewForward();
	}


}
