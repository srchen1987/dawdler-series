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
package com.anywide.dawdler.clientplug.web;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.anywide.dawdler.clientplug.web.handler.ViewControllerContext;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.handler.ViewForward.ResponseType;
import com.anywide.dawdler.clientplug.web.upload.UploadFile;
import com.anywide.dawdler.util.ClassUtil;

/**
 * @author jackson.song
 * @version V1.0
 * @Title TransactionController.java
 * @Description 提供通用的Controller
 * @date 2007年04月17日
 * @email suxuan696@gmail.com
 */
public abstract class TransactionController {
	private ViewForward getViewForward() {
		return ViewControllerContext.getViewForward();
	}

	public HttpServletRequest getRequest() {
		return getViewForward().getRequest();
	}

	public HttpServletResponse getResponse() {
		return getViewForward().getResponse();
	}

	public String getParamsVariable(String key) {
		return getViewForward().getParamsVariable(key);
	}

	public void setData(Map<String, Object> data) {
		getViewForward().setData(data);
	}

	public void putData(String key, Object value) {
		getViewForward().putData(key, value);
	}

	public Object removeData(String key) {
		return getViewForward().removeData(key);
	}

	protected void setErrorPage(String errorPage) {
		getViewForward().setErrorPage(errorPage);
	}

	protected void setTemplatePath(String templatePath) {
		getViewForward().setTemplatePath(templatePath);
	}

	protected void setAddRequestAttribute(boolean addRequestAttribute) {
		getViewForward().setAddRequestAttribute(addRequestAttribute);
	}

	protected void setStatus(ResponseType status) {
		getViewForward().setStatus(status);
	}

	protected void setForwardAndRedirectPath(String forwardAndRedirectPath) {
		getViewForward().setForwardAndRedirectPath(forwardAndRedirectPath);
	}

	public <T> T paramClass(Class<T> classes) {
		Field[] fields = classes.getDeclaredFields();
		Object value = null;
		T instance;
		try {
			instance = classes.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
		for (Field field : fields) {
			Class<?> type = field.getType();
			String fieldName = field.getName();
			if (ClassUtil.isSimpleValueType(type)) {
				value = ClassUtil.convert(paramString(fieldName), type);
			} else if (ClassUtil.isSimpleArrayType(type)) {
				value = ClassUtil.convertArray(paramValues(fieldName), type);
			}

			if (value != null) {
				field.setAccessible(true);
				try {
					field.set(instance, value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
				}
			}
		}
		return instance;
	}

	public int paramInt(String paramname) {
		return getViewForward().paramInt(paramname);
	}

	public int paramInt(String paramname, int defaultvalue) {
		return getViewForward().paramInt(paramname, defaultvalue);
	}

	public long paramLong(String paramname) {
		return getViewForward().paramLong(paramname);
	}

	public long paramLong(String paramname, long value) {
		return getViewForward().paramLong(paramname, value);
	}

	public short paramShort(String paramname) {
		return getViewForward().paramShort(paramname);
	}

	public short paramShort(String paramname, short value) {
		return getViewForward().paramShort(paramname, value);
	}

	public byte paramByte(String paramname) {
		return getViewForward().paramByte(paramname);
	}

	public byte paramByte(String paramname, byte value) {
		return getViewForward().paramByte(paramname, value);
	}

	public float paramFloat(String paramname) {
		return getViewForward().paramFloat(paramname);
	}

	public float paramFloat(String paramname, float value) {
		return getViewForward().paramFloat(paramname, value);
	}

	public double paramDouble(String paramname) {
		return getViewForward().paramDouble(paramname);
	}

	public double paramDouble(String paramname, double value) {
		return getViewForward().paramDouble(paramname, value);
	}

	public boolean paramBoolean(String paramname) {
		return getViewForward().paramBoolean(paramname);
	}

	public Boolean paramObjectBoolean(String paramname) {
		return getViewForward().paramObjectBoolean(paramname);
	}

	public String paramString(String paramname) {
		return getViewForward().paramString(paramname);
	}

	public String paramString(String paramname, String defaultvalue) {
		return getViewForward().paramString(paramname, defaultvalue);
	}

	public Integer paramObjectInt(String paramname) {
		return getViewForward().paramObjectInt(paramname);
	}

	public Long paramObjectLong(String paramname) {
		return getViewForward().paramObjectLong(paramname);
	}

	public Short paramObjectShort(String paramname) {
		return getViewForward().paramObjectShort(paramname);
	}

	public Byte paramObjectByte(String paramname) {
		return getViewForward().paramObjectByte(paramname);
	}

	public Float paramObjectFloat(String paramname) {
		return getViewForward().paramObjectFloat(paramname);
	}

	public Double paramObjectDouble(String paramname) {
		return getViewForward().paramObjectDouble(paramname);
	}

	public String[] paramValues(String paramname) {
		return getViewForward().paramValues(paramname);
	}

	public Map<String, String[]> paramMaps() {
		return getViewForward().paramMaps();
	}

	public List<UploadFile> paramFiles(String paramname) {
		return getViewForward().paramFiles(paramname);
	}

	public UploadFile paramFile(String paramname) {
		return getViewForward().paramFile(paramname);
	}

	public Object getRequestAttribute(String attributename) {
		return getRequest().getAttribute(attributename);
	}

	public void setRequestAttribute(String attributename, Object object) {
		getRequest().setAttribute(attributename, object);
	}

	public void removeRequestAttribute(String attributename) {
		getRequest().removeAttribute(attributename);
	}

	protected void setStatusToError() {
		getViewForward().setStatus(ResponseType.ERROR);
	}

	protected void setStatusToStop() {
		getViewForward().setStatus(ResponseType.STOP);
	}

	protected void setStatusToRedirect() {
		getViewForward().setStatus(ResponseType.REDIRECT);
	}

	protected void setStatusToForward() {
		getViewForward().setStatus(ResponseType.FORWARD);
	}

	protected void setStatusToSuccess() {
		getViewForward().setStatus(ResponseType.SUCCESS);
	}
}
