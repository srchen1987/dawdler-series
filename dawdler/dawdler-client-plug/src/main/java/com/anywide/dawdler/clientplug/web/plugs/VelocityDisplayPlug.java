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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.velocity.VelocityToolBox;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.view.templatemanager.VelocityTemplateManager;
import com.anywide.dawdler.util.DawdlerTool;

/**
 * 
 * @Title: VelocityDisplayPlug.java
 * @Description: velocity的实现
 * @author: jackson.song
 * @date: 2009年06月22日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class VelocityDisplayPlug extends AbstractDisplayPlug {
	private static Logger logger = LoggerFactory.getLogger(VelocityDisplayPlug.class);

	public VelocityDisplayPlug(ServletContext servletContext) {
		super(servletContext);
		InputStream fin = null;
		Properties pstool = new Properties();
		try {
			fin = new FileInputStream(DawdlerTool.getcurrentPath() + "toolboxs.properties");
		} catch (FileNotFoundException e) {
			logger.error("", e);
			fin = null;
		}
		try {
			if (fin != null) {
				pstool.load(fin);
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (fin != null)
				try {
					fin.close();
				} catch (IOException e) {
					logger.error("", e);
				}
		}
		Set<Object> set = pstool.keySet();
		for (Object o : set) {
			String name = o.toString();
			String className = pstool.get(name).toString();
			try {
				Class<?> c = Class.forName(className);
				if (!VelocityToolBox.class.isAssignableFrom(c)) {
					System.err.println("warn\t" + className + "\tmust extends VelocityToolBox!");
					continue;
				}
				try {
					Constructor<?> cs = c.getConstructor(String.class);
					VelocityToolBox obj = (VelocityToolBox) cs.newInstance(name);
					toolboxs.put(name, obj);
				} catch (Exception e) {
					logger.error("", e);
				}
			} catch (ClassNotFoundException e) {
				System.err.println("warn can't find " + className);
				continue;
			}
		}
		pstool = null;
		String templatepath = servletContext.getInitParameter("template-path");
		VelocityTemplateManager tm = VelocityTemplateManager.getInstance();
		Properties ps = new Properties();
		try {
			fin = new FileInputStream(DawdlerTool.getcurrentPath() + "velocity.properties");
		} catch (FileNotFoundException e1) {
			fin = null;
		}
		try {
			if (fin != null) {
				ps.load(fin);
			}
			String path;
			if (templatepath != null && !templatepath.trim().equals(""))
				path = servletContext.getRealPath("WEB-INF/" + templatepath);
			else
				path = servletContext.getRealPath("WEB-INF/template");
			ps.put("file.resource.loader.path", path);
			ps.put("file.resource.loader.cache", "true");
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (fin != null)
				try {
					fin.close();
				} catch (IOException e) {
				}
		}
		tm.init(ps);
	}

	private Map<String, VelocityToolBox> toolboxs = new HashMap<String, VelocityToolBox>();

	public Map<String, VelocityToolBox> getToolboxs() {
		return toolboxs;
	}

	public void setToolboxs(Map<String, VelocityToolBox> toolboxs) {
		this.toolboxs = toolboxs;
	}

	@Override
	public void display(ViewForward wf) {
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
		default:
			break;
		}
		try {
			mergeTemplate(request, response, tpath, wf);
		} catch (Exception e) {
			logger.error("", e);
			return;
		}
	}

	private void mergeTemplate(HttpServletRequest request, HttpServletResponse response, String tpath, ViewForward wf)
			throws IOException, ServletException {
		if (tpath == null)
			throw new ServletException("not set template!");
		PrintWriter out = null;
		try {
			Template template = null;
			try {
				template = VelocityTemplateManager.getInstance().getTemplate(tpath);
			} catch (ResourceNotFoundException e) {
				throw new ServletException(e);
			}
			Map datas = wf.getData();
			Context context = new VelocityContext(datas);
			if (wf.isAddRequestAttribute()) {
				Enumeration<String> attrs = request.getAttributeNames();
				while (attrs.hasMoreElements()) {
					String key = attrs.nextElement();
					Object obj = request.getAttribute(key);
					context.put(key, obj);
				}
			}
			if (!toolboxs.isEmpty()) {
				Set<Entry<String, VelocityToolBox>> vts = toolboxs.entrySet();
				for (Entry<String, VelocityToolBox> en : vts) {
					context.put(en.getKey(), en.getValue());
				}
			}
			String ae = request.getHeader("accept-encoding");
			if (ae != null && ae.indexOf("gzip") != -1) {

				OutputStreamWriter ow = new OutputStreamWriter(new GZIPOutputStream(response.getOutputStream()),
						"UTF-8");
				out = new PrintWriter(ow);
				response.setHeader("Content-Encoding", "gzip");
			} else {
				out = response.getWriter();
			}
			template.merge(context, out);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}
}
