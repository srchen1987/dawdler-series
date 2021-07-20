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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.annotation.RequestMapping.ViewType;
import com.anywide.dawdler.clientplug.annotation.RequestMethod;
import com.anywide.dawdler.clientplug.web.AntPathMatcher;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.ViewControllerContext;
import com.anywide.dawdler.clientplug.web.exception.handler.HttpExceptionHandler;
import com.anywide.dawdler.clientplug.web.exception.handler.HttpExceptionHolder;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AnnotationUrlHandler.java
 * @Description 基于Annotation的UrlHandler实现 基于xml的删除掉了 因为servlet3.0之后不建议使用web.xml了
 * @date 2007年04月18日
 * @email suxuan696@gmail.com
 */
public class AnnotationUrlHandler extends AbstractUrlHandler {
	private static final ConcurrentHashMap<String, RequestUrlData> anturlRules = new ConcurrentHashMap<>(32);

	private static final ConcurrentHashMap<String, RequestUrlData> urlRules = new ConcurrentHashMap<>(64);
	private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

	public static RequestUrlData registMapping(String path, RequestUrlData data) {
		boolean antPath = isAntPath(path);
		if (antPath) {
			return anturlRules.putIfAbsent(path, data);
		} else {
			return urlRules.putIfAbsent(path, data);
		}
	}

	public static RequestUrlData removeMapping(String path) {
		boolean antPath = isAntPath(path);
		if (antPath) {
			return anturlRules.remove(path);
		} else {
			return urlRules.remove(path);
		}
	}

	public boolean handleUrl(String uriShort, String httpMethod, boolean isJson, HttpServletRequest request,
			HttpServletResponse response) {
		Set<Entry<String, RequestUrlData>> rules = urlRules.entrySet();
		if (!isAntPath(uriShort)) {
			RequestUrlData requestUrlData = urlRules.get(uriShort);
			if (requestUrlData == null)
				return false;
			return handleUrl(requestUrlData, uriShort, httpMethod, isJson, null, request, response);
		} else {
			for (Entry<String, RequestUrlData> entry : rules) {
				Map<String, String> variables = new LinkedHashMap<>();
				boolean matched = antPathMatcher.doMatch(entry.getKey(), uriShort, true, variables);
				if (matched) {
					return handleUrl(entry.getValue(), uriShort, httpMethod, isJson, variables, request, response);
				}
			}
			return false;
		}
	}

	private boolean handleUrl(RequestUrlData requestUrlData, String uriShort, String httpMethod, boolean isJson,
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
			} else
				viewForward = new ViewForward(request, response);
		}
		viewForward.setParamsVariable(variables);
		viewForward.setRequestUrlData(requestUrlData);
		viewForward.setUriShort(uriShort);
		ViewControllerContext.setViewForward(viewForward);
		boolean responseBody = requestUrlData.getResponseBody() != null;
		TransactionController targetController = requestUrlData.getTarget();
		Method method = requestUrlData.getMethod();
		try {
			if (multipart) {
				long uploadSizeMax = requestMapping.uploadSizeMax();
				long uploadPerSizeMax = requestMapping.uploadPerSizeMax();
				MultipartViewForward mwf = (MultipartViewForward) viewForward;
				mwf.parse(uploadSizeMax, uploadPerSizeMax);
			}
			if (WebValidateExecutor.validate(request, response, isJson, targetController))
				return invokeMethod(targetController, method, viewForward, responseBody);
			else
				return true;
		} catch (Throwable e) {
			HttpExceptionHandler httpExceptionHandler = null;
			if (exceptionHandler.isEmpty()) {
				exceptionHandler = viewType.toString();
			}
			httpExceptionHandler = HttpExceptionHolder.getHttpExceptionHandler(exceptionHandler);
			if (httpExceptionHandler == null)
				httpExceptionHandler = HttpExceptionHolder.getJsonHttpExceptionHandler();
			httpExceptionHandler.handle(request, response, viewForward, e);
			return true;
		} finally {
			viewForward.release();
			ViewControllerContext.removeViewForward();
		}

	}

	private boolean validateHttpMethods(RequestMapping requestMapping, String httpMethod) {
		RequestMethod[] requestMethods = requestMapping.method();
		if (requestMethods.length == 0)
			return true;
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

	public static Set<TransactionController> getTransactionControllers() {
		Set<TransactionController> controllers = new HashSet<>(32);
		urlRules.values().forEach(requestUrlData -> {
			controllers.add(requestUrlData.getTarget());
		});

		anturlRules.values().forEach(requestUrlData -> {
			controllers.add(requestUrlData.getTarget());
		});
		return controllers;
	}

	// FIXME no implement
//	private boolean validateHeaders(RequestMapping requestMapping) throws ServletException {
//		// String[] headers = requestMapping.headers();
//		return true;
//	}
}
