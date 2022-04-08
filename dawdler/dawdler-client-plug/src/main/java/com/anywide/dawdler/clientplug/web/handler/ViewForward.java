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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.web.upload.UploadFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ViewForward.java
 * @Description viewForward定义父类
 * @date 2007年4月18日
 * @email suxuan696@gmail.com
 */
public class ViewForward {
	private String errorPage = "error.html";// 错误地址
	private String templatePath;// 模板路径
	private boolean addRequestAttribute = false;// 自动添加request请求范围
	private String forwardAndRedirectPath;// 跳转路径
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	private ResponseType status = ResponseType.SUCCESS;// 状态默认为成功
	private Map<String, Object> data = null;
	private Map<String, String> paramsVariable;
	private boolean createContext;
	private Throwable invokeException;
	private RequestUrlData requestUrlData;
	private String uriShort;
	private String antPath;

	public ViewForward(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	public String getUriShort() {
		return uriShort;
	}

	void setUriShort(String uriShort) {
		this.uriShort = uriShort;
	}

	public String getAntPath() {
		return antPath;
	}

	public void setAntPath(String antPath) {
		this.antPath = antPath;
	}

	void setRequestUrlData(RequestUrlData requestUrlData) {
		this.requestUrlData = requestUrlData;
	}

	public boolean isCreateontext() {
		return createContext;
	}

	void setCreateontext(boolean createContext) {
		this.createContext = createContext;
	}

	public Object getTransactionController() {
		return requestUrlData.getTarget();
	}

	public RequestMapping getRequestMapping() {
		return requestUrlData.getRequestMapping();
	}

	private void createData() {
		createContext = true;
		data = new HashMap<String, Object>();
	}

	public Throwable getInvokeException() {
		return invokeException;
	}

	public void setInvokeException(Throwable invokeException) {
		this.invokeException = invokeException;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public String getForwardAndRedirectPath() {
		return forwardAndRedirectPath;
	}

	public void setForwardAndRedirectPath(String forwardAndRedirectPath) {
		this.forwardAndRedirectPath = forwardAndRedirectPath;
	}

	public boolean isAddRequestAttribute() {
		return addRequestAttribute;
	}

	public void setAddRequestAttribute(boolean addRequestAttribute) {
		this.addRequestAttribute = addRequestAttribute;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public ResponseType getStatus() {
		return status;
	}

	public void setStatus(ResponseType status) {
		this.status = status;
	}

	public String getErrorPage() {
		return errorPage;
	}

	public void setErrorPage(String errorPage) {
		this.errorPage = errorPage;
	}

	public void putData(String key, Object value) {
		if (!createContext)
			createData();
		if (!data.containsKey(key))
			data.put(key, value);
	}

	public Object removeData(String key) {
		return data.remove(key);
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public String getHeader(String headerName) {
		return getRequest().getHeader(headerName);
	}

	public String getServiceType() {
		return getRequestMapping().viewType().name();
	}

	public int paramInt(String paramName) {
		try {
			return Integer.parseInt(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return 0;
		}
	}

	public int paramInt(String paramName, int defaultvalue) {
		try {
			return Integer.parseInt(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return defaultvalue;
		}
	}

	public long paramLong(String paramName) {
		try {
			return Long.parseLong(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return 0;
		}
	}

	public long paramLong(String paramName, long value) {
		try {
			return Long.parseLong(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return value;
		}
	}

	public short paramShort(String paramName) {
		try {
			return Short.parseShort(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return 0;
		}
	}

	public short paramShort(String paramName, short value) {
		try {
			return Short.parseShort(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return value;
		}
	}

	public byte paramByte(String paramName) {
		try {
			return Byte.parseByte(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return 0;
		}
	}

	public byte paramByte(String paramName, byte value) {
		try {
			return Byte.parseByte(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return value;
		}
	}

	public float paramFloat(String paramName, float value) {
		try {
			return Float.parseFloat(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return value;
		}
	}

	public float paramFloat(String paramName) {
		try {
			return Float.parseFloat(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return 0.0f;
		}
	}

	public double paramDouble(String paramName) {
		try {
			return Double.parseDouble(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return 0.00d;
		}
	}

	public double paramDouble(String paramName, double value) {
		try {
			return Double.parseDouble(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return value;
		}
	}

	public boolean paramBoolean(String paramName) {
		try {
			return Boolean.parseBoolean(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return false;
		}
	}

	public String paramString(String paramName) {
		return getRequest().getParameter(paramName);
	}

	public String paramString(String paramName, String defaultvalue) {
		String value = getRequest().getParameter(paramName);
		if (value == null)
			return defaultvalue;
		return value;
	}

	public Integer paramObjectInt(String paramName) {
		try {
			return Integer.parseInt(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return null;
		}
	}

	public Long paramObjectLong(String paramName) {
		try {
			return Long.parseLong(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return null;
		}
	}

	public Short paramObjectShort(String paramName) {
		try {
			return Short.parseShort(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return null;
		}
	}

	public Byte paramObjectByte(String paramName) {
		try {
			return Byte.parseByte(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return null;
		}
	}

	public Float paramObjectFloat(String paramName) {
		try {
			return Float.parseFloat(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return null;
		}
	}

	public Double paramObjectDouble(String paramName) {
		try {
			return Double.parseDouble(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return null;
		}
	}

	public Boolean paramObjectBoolean(String paramName) {
		try {
			return Boolean.parseBoolean(getRequest().getParameter(paramName));
		} catch (Exception e) {
			return null;
		}
	}

	public String[] paramValues(String paramName) {
		return getRequest().getParameterValues(paramName);
	}

	public Map<String, String[]> paramMaps() {
		return new HashMap<>(getRequest().getParameterMap());
	}

	public List<UploadFile> paramFiles(String paramName) {
		return null;
	}

	public UploadFile paramFile(String paramName) {
		return null;
	}

	public String getParamsVariable(String key) {
		return paramsVariable != null ? paramsVariable.get(key) : null;
	}

	public void setParamsVariable(Map<String, String> paramsVariable) {
		this.paramsVariable = paramsVariable;
	}

	public void release() {
		if (data != null)
			data.clear();
		if (paramsVariable != null)
			paramsVariable.clear();
	}

	public enum ResponseType {
		SUCCESS, ERROR, REDIRECT, FORWARD, STOP
	}
}
