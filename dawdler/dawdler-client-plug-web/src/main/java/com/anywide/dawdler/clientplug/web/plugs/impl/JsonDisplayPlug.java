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
package com.anywide.dawdler.clientplug.web.plugs.impl;

import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.annotation.RequestMapping.ViewType;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.handler.ViewForward.ResponseType;
import com.anywide.dawdler.clientplug.web.plugs.AbstractDisplayPlug;
import com.anywide.dawdler.util.JsonProcessUtil;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JsonDisplayPlug.java
 * @Description json的实现
 * @date 2007年4月21日
 * @email suxuan696@gmail.com
 */
public class JsonDisplayPlug extends AbstractDisplayPlug {
	private static final Logger logger = LoggerFactory.getLogger(JsonDisplayPlug.class);

	@Override
	public void display(ViewForward wf) {
		logException(wf);
		HttpServletRequest request = wf.getRequest();
		HttpServletResponse response = wf.getResponse();
		response.setContentType(MIME_TYPE_JSON);
		String json = null;
		if (wf.getInvokeException() != null) {
			wf.setStatus(ResponseType.ERROR);
		}
		switch (wf.getStatus()) {
		case SUCCESS: {
			if (wf.isAddRequestAttribute()) {
				Enumeration<String> attrs = request.getAttributeNames();
				while (attrs.hasMoreElements()) {
					String key = attrs.nextElement();
					Object obj = request.getAttribute(key);
					wf.putData(key, obj);
				}
			}
			if (wf.getData() != null) {
				if (wf.isJsonIgnoreNull()) {
					json = JsonProcessUtil.ignoreNullBeanToJson(wf.getData());
				} else {
					json = JsonProcessUtil.beanToJson(wf.getData());
				}
			}
			break;
		}
		case ERROR: {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			wf.putData("msg", "Internal Server Error!");
			json = JsonProcessUtil.beanToJson(wf.getData());
			break;
		}
		case REDIRECT:
		case FORWARD:
		case STOP:
			return;
		}
		if (json != null) {
			print(response, json);
		}
	}

	private void print(HttpServletResponse response, String message) {
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.write(message);
			out.flush();
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	@Override
	public void init(ServletContext servletContext) {

	}

	@Override
	public String plugName() {
		return ViewType.json.toString();
	}

}
