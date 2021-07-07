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
package com.anywide.dawdler.clientplug.web.plugs;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.handler.ViewForward;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JspDisplayPlug.java
 * @Description jsp的实现
 * @date 2007年04月21日
 * @email suxuan696@gmail.com
 */
public class JspDisplayPlug extends AbstractDisplayPlug {
	private static final Logger logger = LoggerFactory.getLogger(JspDisplayPlug.class);

	private final String path;

	public JspDisplayPlug(ServletContext servletContext) {
		super(servletContext);
		String templatePath = servletContext.getInitParameter("template-path");
		if (templatePath != null && !templatePath.trim().equals(""))
			path = "/WEB-INF/" + templatePath + "/";
		else
			path = "/WEB-INF/template/";
	}

	@Override
	public void display(ViewForward wf) {
		logException(wf);
		HttpServletRequest request = wf.getRequest();
		HttpServletResponse response = wf.getResponse();
		response.setContentType(MIME_TYPE_TEXT);
		if (wf.getInvokeException() != null) {
			logger.error("", wf.getInvokeException());
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error.");
			} catch (IOException e) {
				logger.error("", e);
			}
			return;
		}
		String tpath = null;
		switch (wf.getStatus()) {
		case SUCCESS:
			tpath = wf.getTemplatePath();
			break;
		case ERROR:
			tpath = wf.getErrorPage();
			break;
		case REDIRECT:
			tpath = wf.getForwardAndRedirectPath();
			try {
				response.sendRedirect(tpath);
			} catch (IOException e) {
				logger.error("", e);
			}
			return;
		case FORWARD:
			tpath = wf.getForwardAndRedirectPath();
			try {
				request.getRequestDispatcher(tpath).forward(request, response);
			} catch (ServletException | IOException e) {
				logger.error("", e);
			}
			return;
		case STOP:
			return;
		}
		try {
			Map<String, Object> map = wf.getData();
			if (map != null) {
				Set<Entry<String, Object>> entrys = map.entrySet();
				for (Entry<String, Object> entry : entrys) {
					request.setAttribute(entry.getKey(), entry.getValue());
				}
			}
			request.getRequestDispatcher(path + tpath).forward(request, response);
		} catch (ServletException | IOException e) {
			logger.error("", e);
		}
	}
}
