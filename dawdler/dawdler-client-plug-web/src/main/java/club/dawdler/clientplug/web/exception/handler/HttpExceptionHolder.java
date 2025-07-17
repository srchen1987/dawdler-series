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
package club.dawdler.clientplug.web.exception.handler;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.clientplug.web.annotation.RequestMapping.ViewType;
import club.dawdler.clientplug.web.handler.ViewForward;
import club.dawdler.clientplug.web.plugs.DisplaySwitcher;

/**
 * @author jackson.song
 * @version V1.0
 * 异常处理持有者，可以注册，提供json默认的处理方式
 */
public class HttpExceptionHolder {
	private HttpExceptionHolder() {
	}

	private static final Logger logger = LoggerFactory.getLogger(HttpExceptionHolder.class);
	private static final ConcurrentHashMap<String, HttpExceptionHandler> HANDLES = new ConcurrentHashMap<>();

	static {
		HANDLES.put(ViewType.json.toString(), new JsonHttpExceptionHandler());
		HANDLES.put(ViewType.velocity.toString(), new VelocityHttpExceptionHandler());
		HANDLES.put(ViewType.jsp.toString(), new JspHttpExceptionHandler());
	}

	public static void register(String id, HttpExceptionHandler handler) {
		HttpExceptionHandler handlerPre = HANDLES.putIfAbsent(id, handler);
		if (handlerPre != null) {
			logger.warn(handler.getClass().getName() + " : " + id + "\talready exists!");
		}
	}

	public static HttpExceptionHandler getHttpExceptionHandler(String id) {
		return HANDLES.get(id);
	}

	public static HttpExceptionHandler getJsonHttpExceptionHandler() {
		return getHttpExceptionHandler(ViewType.json.toString());
	}

	public static class JsonHttpExceptionHandler implements HttpExceptionHandler {
		@Override
		public void handle(HttpServletRequest request, HttpServletResponse response, ViewForward viewForward,
				Throwable ex) {
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
