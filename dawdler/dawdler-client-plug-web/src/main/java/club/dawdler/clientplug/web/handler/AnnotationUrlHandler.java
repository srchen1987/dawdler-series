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
package club.dawdler.clientplug.web.handler;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import club.dawdler.clientplug.web.AntPathMatcher;
import club.dawdler.clientplug.web.annotation.RequestMapping;
import club.dawdler.clientplug.web.annotation.RequestMapping.ViewType;
import club.dawdler.clientplug.web.exception.ConvertException;
import club.dawdler.clientplug.web.exception.handler.HttpExceptionHandler;
import club.dawdler.clientplug.web.exception.handler.HttpExceptionHolder;
import club.dawdler.clientplug.web.plugs.PlugFactory;
import club.dawdler.clientplug.web.validator.exception.ValidationException;
import club.dawdler.util.spring.MediaType;
import club.dawdler.util.spring.MimeType;

/**
 * @author jackson.song
 * @version V1.0
 * 基于Annotation的UrlHandler实现 基于xml的删除掉了 因为servlet3.0之后不建议使用web.xml了
 */
public class AnnotationUrlHandler extends AbstractUrlHandler {
	private static final ConcurrentHashMap<String, PathMappingData> ANT_URL_RULES = new ConcurrentHashMap<>(64);

	private static final ConcurrentHashMap<String, PathMappingData> URL_RULES = new ConcurrentHashMap<>(128);
	private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

	public static RequestUrlData registMapping(String path, RequestUrlData data) {
		ConcurrentHashMap<String, PathMappingData> rules = isAntPath(path) ? ANT_URL_RULES : URL_RULES;
		PathMappingData pathMappingData = rules.computeIfAbsent(path, key -> new PathMappingData());
		return pathMappingData.put(data);
	}

	public static boolean removeMapping(String path, RequestUrlData data) {
		ConcurrentHashMap<String, PathMappingData> rules = isAntPath(path) ? ANT_URL_RULES : URL_RULES;
		PathMappingData pathMappingData = rules.get(path);
		if (pathMappingData == null) {
			return false;
		}
		boolean removed = pathMappingData.remove(data);
		if (removed && pathMappingData.isEmpty()) {
			rules.remove(path, pathMappingData);
		}
		return removed;
	}

	@Override
	public boolean handleUrl(String uriShort, String httpMethod, HttpServletRequest request,
			HttpServletResponse response) {
		PathMappingData pathMappingData = URL_RULES.get(uriShort);
		if (pathMappingData != null) {
			RequestUrlData requestUrlData = pathMappingData.match(httpMethod);
			if (requestUrlData != null) {
				return handleUrl(requestUrlData, uriShort, null, httpMethod, null, request, response);
			}
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return true;
		}
		Set<Entry<String, PathMappingData>> rules = ANT_URL_RULES.entrySet();
		List<Entry<String, PathMappingData>> matchedEntries = null;
		Map<String, String> variables = new HashMap<>(8);
		Map<String, String> matchedVariables = null;
		for (Entry<String, PathMappingData> entry : rules) {
			variables.clear();
			boolean matched = ANT_PATH_MATCHER.doMatch(entry.getKey(), uriShort, true, variables);
			if (matched) {
				if (matchedEntries == null) {
					matchedEntries = new ArrayList<>(8);
					matchedVariables = new HashMap<>(variables);
				}
				matchedEntries.add(entry);
			}
		}
		if (matchedEntries == null) {
			return false;
		}
		Entry<String, PathMappingData> bestEntry;
		if (matchedEntries.size() == 1) {
			bestEntry = matchedEntries.get(0);
		} else {
			bestEntry = matchedEntries.stream()
					.min(Comparator.comparing(Entry::getKey, ANT_PATH_MATCHER.getPatternComparator(uriShort)))
					.orElse(matchedEntries.get(0));
			matchedVariables.clear();
			ANT_PATH_MATCHER.doMatch(bestEntry.getKey(), uriShort, true, matchedVariables);
		}
		RequestUrlData requestUrlData = bestEntry.getValue().match(httpMethod);
		if (requestUrlData != null) {
			return handleUrl(requestUrlData, uriShort, bestEntry.getKey(), httpMethod, matchedVariables, request,
					response);
		}
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		return true;
	}

	private boolean handleUrl(RequestUrlData requestUrlData, String uriShort, String antPath, String httpMethod,
			Map<String, String> variables, HttpServletRequest request, HttpServletResponse response) {
		RequestMapping requestMapping = requestUrlData.getRequestMapping();
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
			if (!applyProduces(requestMapping, request, response)) {
				return true;
			}
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

	private static boolean isAntPath(String uri) {
		return (uri.indexOf("{") != -1) || (uri.indexOf("?") != -1) || (uri.indexOf("*") != -1);
	}

	private boolean applyProduces(RequestMapping requestMapping, HttpServletRequest request,
			HttpServletResponse response) {
		String[] produces = requestMapping.produces();
		if (produces.length == 0) {
			return true;
		}
		List<MimeType> producibleTypes = new ArrayList<>(produces.length);
		for (String produce : produces) {
			if (produce != null && !produce.trim().isEmpty()) {
				producibleTypes.add(MimeType.valueOf(produce.trim()));
			}
		}
		if (producibleTypes.isEmpty()) {
			return true;
		}
		String acceptHeader = request.getHeader("Accept");
		List<MimeType> acceptableTypes;
		if (acceptHeader == null || acceptHeader.trim().isEmpty()) {
			acceptableTypes = Collections.singletonList(MimeType.valueOf(MediaType.ALL_VALUE));
		} else {
			acceptableTypes = MimeType.parseMediaTypes(acceptHeader);
			acceptableTypes.sort(new Comparator<MimeType>() {
				@Override
				public int compare(MimeType a, MimeType b) {
					int result = Double.compare(b.getQualityValue(), a.getQualityValue());
					if (result != 0) {
						return result;
					}
					if (a.isWildcardType() && !b.isWildcardType()) {
						return 1;
					} else if (!a.isWildcardType() && b.isWildcardType()) {
						return -1;
					}
					if (a.isWildcardSubtype() && !b.isWildcardSubtype()) {
						return 1;
					} else if (!a.isWildcardSubtype() && b.isWildcardSubtype()) {
						return -1;
					}
					return 0;
				}
			});
		}
		for (MimeType acceptable : acceptableTypes) {
			if (acceptable.getQualityValue() <= 0D) {
				continue;
			}
			for (MimeType producible : producibleTypes) {
				if (producible.isCompatibleWith(acceptable)) {
					response.setContentType(producible.toString());
					return true;
				}
			}
		}
		response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
		return false;
	}

	public static Set<Object> getTransactionControllers() {
		Set<Object> controllers = new HashSet<>(32);
		URL_RULES.values().forEach(pathMappingData -> {
			pathMappingData.allRequestUrlData().forEach(requestUrlData -> {
				controllers.add(requestUrlData.getTarget());
			});
		});

		ANT_URL_RULES.values().forEach(pathMappingData -> {
			pathMappingData.allRequestUrlData().forEach(requestUrlData -> {
				controllers.add(requestUrlData.getTarget());
			});
		});
		return controllers;
	}

}
