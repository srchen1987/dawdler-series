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
package com.anywide.dawdler.clientplug.web.exception.handler;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.annotation.RequestMapping.ViewType;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.handler.ViewForward.ResponseType;
import com.anywide.dawdler.clientplug.web.plugs.DisplaySwitcher;

/**
 * @author jackson.song
 * @version V1.0
 * @Title HttpExceptionHolder.java
 * @Description 异常处理持有者，可以注册，提供json默认的处理方式
 * @date 2007年4月23日
 * @email suxuan696@gmail.com
 */
public class HttpExceptionHolder {
	private static final Logger logger = LoggerFactory.getLogger(HttpExceptionHolder.class);
	private static final ConcurrentHashMap<String, HttpExceptionHandler> handles = new ConcurrentHashMap<>();

	static {
		handles.put(ViewType.json.toString(), new JsonHttpExceptionHandler());
		handles.put(ViewType.velocity.toString(), new VelocityHttpExceptionHandler());
		handles.put(ViewType.jsp.toString(), new JspHttpExceptionHandler());
	}

	public static void regist(String id, HttpExceptionHandler handler) {
		HttpExceptionHandler handlerPre = handles.putIfAbsent(id, handler);
		if (handlerPre != null) {
			logger.warn(handler.getClass().getName() + " : " + id + "\talready exists!");
		}
	}

	public static HttpExceptionHandler getHttpExceptionHandler(String id) {
		return handles.get(id);
	}

	public static HttpExceptionHandler getJsonHttpExceptionHandler() {
		return getHttpExceptionHandler(ViewType.json.toString());
	}

	public static class JsonHttpExceptionHandler implements HttpExceptionHandler {
		@Override
		public void handle(HttpServletRequest request, HttpServletResponse response, ViewForward viewForward,
				Throwable ex) {
			logger.error("", ex);// 如果不想输出这个，可以注册自己的handler，也可以注释源码
			response.setStatus(500);
			viewForward.setStatus(ResponseType.ERROR);
			viewForward.putData("success", false);
			DisplaySwitcher.switchDisplay(viewForward);
		}
	}
	
	public static class JspHttpExceptionHandler implements HttpExceptionHandler {
		@Override
		public void handle(HttpServletRequest request, HttpServletResponse response, ViewForward viewForward,
				Throwable ex) {
			DisplaySwitcher.switchDisplay(viewForward);
		}
	}
	
	
	public static class VelocityHttpExceptionHandler implements HttpExceptionHandler {
		@Override
		public void handle(HttpServletRequest request, HttpServletResponse response, ViewForward viewForward,
				Throwable ex) {
			DisplaySwitcher.switchDisplay(viewForward);
		}
	}
}
