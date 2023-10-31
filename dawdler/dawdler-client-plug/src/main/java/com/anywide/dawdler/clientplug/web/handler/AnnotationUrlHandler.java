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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.annotation.RequestMapping.RequestMethod;
import com.anywide.dawdler.clientplug.annotation.RequestMapping.ViewType;
import com.anywide.dawdler.clientplug.web.AntPathMatcher;
import com.anywide.dawdler.clientplug.web.exception.ConvertException;
import com.anywide.dawdler.clientplug.web.exception.handler.HttpExceptionHandler;
import com.anywide.dawdler.clientplug.web.exception.handler.HttpExceptionHolder;
import com.anywide.dawdler.clientplug.web.plugs.PlugFactory;
import com.anywide.dawdler.clientplug.web.validator.exception.ValidationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AnnotationUrlHandler.java
 * @Description 基于Annotation的UrlHandler实现 基于xml的删除掉了 因为servlet3.0之后不建议使用web.xml了
 * @date 2007年4月18日
 * @email suxuan696@gmail.com
 */
public class AnnotationUrlHandler extends AbstractUrlHandler {
	private static final ConcurrentHashMap<String, RequestUrlData> ANT_URL_RULES = new ConcurrentHashMap<>(64);

	private static final ConcurrentHashMap<String, RequestUrlData> URL_RULES = new ConcurrentHashMap<>(128);
	private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

	public static RequestUrlData registMapping(String path, RequestUrlData data) {
		boolean antPath = isAntPath(path);
		if (antPath) {
			return ANT_URL_RULES.putIfAbsent(path, data);
		} else {
			return URL_RULES.putIfAbsent(path, data);
		}
	}

	public static RequestUrlData removeMapping(String path) {
		boolean antPath = isAntPath(path);
		if (antPath) {
			return ANT_URL_RULES.remove(path);
		} else {
			return URL_RULES.remove(path);
		}
	}

	@Override
	public boolean handleUrl(String uriShort, String httpMethod, HttpServletRequest request,
			HttpServletResponse response) {
		RequestUrlData requestUrlData = URL_RULES.get(uriShort);
		if (requestUrlData != null) {
			return handleUrl(requestUrlData, uriShort, null, httpMethod, null, request, response);
		}
		Set<Entry<String, RequestUrlData>> rules = ANT_URL_RULES.entrySet();
		Map<String, String> variables = new HashMap<>(8);
		for (Entry<String, RequestUrlData> entry : rules) {
			boolean matched = ANT_PATH_MATCHER.doMatch(entry.getKey(), uriShort, true, variables);
			if (matched) {
				return handleUrl(entry.getValue(), uriShort, entry.getKey(), httpMethod, variables, request, response);
			}
		}
		return false;
	}

	private boolean handleUrl(RequestUrlData requestUrlData, String uriShort, String antPath, String httpMethod,
			Map<String, String> variables, HttpServletRequest request, HttpServletResponse response) {
		RequestMapping requestMapping = requestUrlData.getRequestMapping();
		if (!validateHttpMethods(requestMapping, httpMethod)) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return true;
		}
		boolean multipart = ServletFileUpload.isMultipartContent(request);
		ViewForward viewForward = createViewForward();
		String exceptionHandler = requestMapping.exceptionHandler();
		ViewType viewType = requestMapping.viewType();
		if (viewForward == null) {
			if (multipart) {
				viewForward = new MultipartViewForward(request, response);
			} else {
				viewForward = new ViewForward(request, response);
			}
		}
		viewForward.setParamsVariable(variables);
		viewForward.setRequestUrlData(requestUrlData);
		viewForward.setUriShort(uriShort);
		viewForward.setAntPath(antPath);
		ViewControllerContext.setViewForward(viewForward);
		boolean responseBody = requestUrlData.getResponseBody() != null;
		Object targetController = requestUrlData.getTarget();
		Method method = requestUrlData.getMethod();
		try {
			if (multipart) {
				long uploadSizeMax = requestMapping.uploadSizeMax();
				long uploadPerSizeMax = requestMapping.uploadPerSizeMax();
				MultipartViewForward mwf = (MultipartViewForward) viewForward;
				mwf.parse(uploadSizeMax, uploadPerSizeMax);
			}
			if (WebValidateExecutor.validate(request, response, targetController, viewForward)) {
				try {
					return invokeMethod(targetController, method, requestMapping, viewForward, responseBody);
				} catch (ValidationException e) {
					Map<String, String> errors = new HashMap<>(16);
					errors.put(e.getFieldName(), e.getError());
					if (requestMapping != null && requestMapping.input() != null
							&& !requestMapping.input().trim().equals("")) {
						request.setAttribute(WebValidateExecutor.VALIDATE_ERROR, errors);
						request.getRequestDispatcher(requestMapping.input()).forward(request, response);
					} else {
						viewForward.putData(WebValidateExecutor.VALIDATE_ERROR, errors);
						PlugFactory.getDisplayPlug(ViewType.json.toString()).display(viewForward);
					}
					return true;
				} catch (ConvertException e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					PrintWriter out = response.getWriter();
					out.write(e.getMessage());
					out.flush();
					out.close();
				}
			}
			return true;
		} catch (Throwable e) {
			viewForward.setInvokeException(e);
			if (exceptionHandler.isEmpty()) {
				exceptionHandler = viewType.toString();
			}
			HttpExceptionHandler httpExceptionHandler = HttpExceptionHolder.getHttpExceptionHandler(exceptionHandler);
			if (httpExceptionHandler == null) {
				httpExceptionHandler = HttpExceptionHolder.getJsonHttpExceptionHandler();
			}
			httpExceptionHandler.handle(request, response, viewForward, e);
			return true;
		} finally {
			viewForward.release();
			ViewControllerContext.removeViewForward();
		}

	}

	private boolean validateHttpMethods(RequestMapping requestMapping, String httpMethod) {
		RequestMethod[] requestMethods = requestMapping.method();
		if (requestMethods.length == 0) {
			return true;
		}
		for (RequestMethod requestMethod : requestMethods) {
			if (requestMethod.equals(RequestMethod.valueOf(httpMethod))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isAntPath(String uri) {
		return (uri.indexOf("{") != -1) || (uri.indexOf("?") != -1) || (uri.indexOf("*") != -1);
	}

	public static Set<Object> getTransactionControllers() {
		Set<Object> controllers = new HashSet<>(32);
		URL_RULES.values().forEach(requestUrlData -> {
			controllers.add(requestUrlData.getTarget());
		});

		ANT_URL_RULES.values().forEach(requestUrlData -> {
			controllers.add(requestUrlData.getTarget());
		});
		return controllers;
	}

}
