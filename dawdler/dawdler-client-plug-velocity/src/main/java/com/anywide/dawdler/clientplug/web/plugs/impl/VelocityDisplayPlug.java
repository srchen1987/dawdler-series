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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.velocity.VelocityToolBox;
import com.anywide.dawdler.clientplug.web.annotation.RequestMapping.ViewType;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.plugs.AbstractDisplayPlug;
import com.anywide.dawdler.clientplug.web.view.templatemanager.VelocityTemplateManager;
import com.anywide.dawdler.util.PropertiesUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author jackson.song
 * @version V1.0
 * velocity的实现
 */
public class VelocityDisplayPlug extends AbstractDisplayPlug {
	private static final Logger logger = LoggerFactory.getLogger(VelocityDisplayPlug.class);
	private Map<String, VelocityToolBox> toolBoxes = new HashMap<>();

	public Map<String, VelocityToolBox> getToolBoxes() {
		return toolBoxes;
	}

	public void setToolBoxes(Map<String, VelocityToolBox> toolBoxes) {
		this.toolBoxes = toolBoxes;
	}

	@Override
	public void display(ViewForward wf) {
		logException(wf);
		HttpServletRequest request = wf.getRequest();
		HttpServletResponse response = wf.getResponse();
		response.setContentType(MIME_TYPE_TEXT_HTML);
		if (wf.getInvokeException() != null) {
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error!");
			} catch (IOException e) {
				logger.error("", e);
			}
			return;
		}
		String tPath = null;
		switch (wf.getStatus()) {
		case SUCCESS:
			tPath = wf.getTemplatePath();
			break;
		case ERROR:
			tPath = wf.getErrorPage();
			break;
		case REDIRECT:
			tPath = wf.getForwardAndRedirectPath();
			try {
				response.sendRedirect(tPath);
			} catch (IOException e) {
				logger.error("", e);
			}
			return;
		case FORWARD:
			tPath = wf.getForwardAndRedirectPath();
			try {
				request.getRequestDispatcher(tPath).forward(request, response);
			} catch (ServletException | IOException e) {
				logger.error("", e);
			}
			return;
		case STOP:
			return;
		default:
			break;
		}
		try {
			mergeTemplate(request, response, tPath, wf);
		} catch (Exception e) {
			logger.error("", e);
			return;
		}
	}

	private void mergeTemplate(HttpServletRequest request, HttpServletResponse response, String tpath, ViewForward wf)
			throws IOException, ServletException {
		if (tpath == null) {
			throw new ServletException("not set template!");
		}
		PrintWriter out = null;
		try {
			Template template = VelocityTemplateManager.getInstance().getTemplate(tpath);
			Map<String, Object> data = wf.getData();
			Context context = new VelocityContext(data);
			if (wf.isAddRequestAttribute()) {
				Enumeration<String> attrs = request.getAttributeNames();
				while (attrs.hasMoreElements()) {
					String key = attrs.nextElement();
					Object obj = request.getAttribute(key);
					context.put(key, obj);
				}
			}
			if (!toolBoxes.isEmpty()) {
				Set<Entry<String, VelocityToolBox>> vts = toolBoxes.entrySet();
				for (Entry<String, VelocityToolBox> en : vts) {
					context.put(en.getKey(), en.getValue());
				}
			}
			String ae = request.getHeader("accept-encoding");
			if (ae != null && ae.indexOf("gzip") != -1) {
				out = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(response.getOutputStream()),
						StandardCharsets.UTF_8));
				response.setHeader("Content-Encoding", "gzip");
			} else {
				out = response.getWriter();
			}
			template.merge(context, out);
			out.flush();
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	@Override
	public void init(ServletContext servletContext) {
		Properties psTool = null;
		try {
			psTool = PropertiesUtil.loadActiveProfileIfNotExistUseDefaultProperties("toolBoxes");
		} catch (Exception e) {
			logger.error("", e);
			return;
		}
		Set<Object> set = psTool.keySet();
		for (Object o : set) {
			String name = o.toString();
			String className = psTool.get(name).toString();
			try {
				Class<?> c = Class.forName(className);
				if (!VelocityToolBox.class.isAssignableFrom(c)) {
					System.err.println("warn\t" + className + "\tmust extends VelocityToolBox!");
					continue;
				}
				Constructor<?> cs = c.getConstructor(String.class);
				VelocityToolBox obj = (VelocityToolBox) cs.newInstance(name);
				toolBoxes.put(name, obj);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		VelocityTemplateManager tm = VelocityTemplateManager.getInstance();

		Properties ps = null;
		try {
			ps = PropertiesUtil.loadProperties("velocity");
		} catch (Exception e) {
			ps = new Properties();
		}
		ps.setProperty(RuntimeConstants.RESOURCE_LOADERS, "class");
        ps.setProperty("resource.loader.class.class", ClasspathResourceLoader.class.getName());
		tm.init(ps);

	}

	@Override
	public String plugName() {
		return ViewType.velocity.toString();
	}
}
