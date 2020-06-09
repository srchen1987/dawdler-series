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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.annotation.RequestMethod;
import com.anywide.dawdler.clientplug.web.AntPathMatcher;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.ViewControllerContext;
import com.anywide.dawdler.clientplug.web.upload.FileUploadExceptionHandler;
import com.anywide.dawdler.clientplug.web.upload.FileUploadExceptionHolder;
/**
 * 
 * @Title:  AnnotationUrlHandler.java   
 * @Description:    基于Annotation的UrlHandler实现 基于xml的删除掉了 因为servlet3.0之后不建议使用web.xml了   
 * @author: jackson.song    
 * @date:   2007年04月18日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class AnnotationUrlHandler extends AbstractUrlHandler {
	private static Logger logger = LoggerFactory.getLogger(AnnotationUrlHandler.class);
	private static ConcurrentHashMap<String, RequestUrlData> urlRules = new ConcurrentHashMap<String, RequestUrlData>();
	private static AntPathMatcher antPathMatcher = new AntPathMatcher();
	public boolean handleUrl(String urishort, String httpMethod,
			HttpServletRequest request, HttpServletResponse response) throws ServletException{
		Set<Entry<String, RequestUrlData>> set = urlRules.entrySet();
		Map<String, String> variables = new LinkedHashMap<String, String>();
		for (Entry<String, RequestUrlData> entry : set) {
			boolean matched  = antPathMatcher.doMatch(entry.getKey(), urishort, true, variables);
			if(matched) {
				RequestUrlData requestUrlData = entry.getValue();
				RequestMapping requestMapping = requestUrlData
						.getRequestMapping();
				if (!validateHttpMethods(requestMapping, httpMethod)) {
					response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
					return true;
				}
				boolean multipart = ServletFileUpload.isMultipartContent(request);
				ViewForward wf = createViewForward();
				if(wf==null) {
//					if(requestUrlData.getRequestMapping().paramType()==ParamType.httpType) {
//						wf = new ViewForward();
//					}else {
//						wf = new JsonWiewForward();
//						wf.set
//					}
					if(multipart) {
						try {
							long uploadSizeMax = requestMapping.uploadSizeMax();
							long uploadPerSizeMax = requestMapping.uploadPerSizeMax();
							wf = new MultipartViewForward(request,response,uploadSizeMax,uploadPerSizeMax);
						} catch (FileUploadException e) {
							logger.error("",e);
							wf = new ViewForward(request,response);
							wf.setParamsVariable(variables);
							wf.setRequestUrlData(requestUrlData);
							wf.setUriShort(urishort);
							ViewControllerContext.setViewForward(wf);
							FileUploadExceptionHandler fileUploadExceptionHandler = FileUploadExceptionHolder.getFileUploadExceptionHandler(FileUploadExceptionHolder.JSON);
							if(fileUploadExceptionHandler==null) 
								fileUploadExceptionHandler = FileUploadExceptionHolder.getJsonFileUploadExceptionHandler();
							fileUploadExceptionHandler.handle(request, response, wf,e);
							return true;
						}
					}else
						wf = new ViewForward(request,response);
				}
				wf.setParamsVariable(variables);
				wf.setRequestUrlData(requestUrlData);
				wf.setUriShort(urishort);
				ViewControllerContext.setViewForward(wf);
				
				TransactionController targetobj = requestUrlData.getTarget();
				Method method = requestUrlData.getMethod();
				try {
					if(WebValidateExecutor.validate(request, response,targetobj))
						return invokeMethod(targetobj, method, wf);
					else return true;
				} finally {
					wf.release();
					ViewControllerContext.removeViewForward();	
				}
			}
		}
		return false;
	}

	private boolean validateHttpMethods(RequestMapping requestMapping,
			String httpMethod) {
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
	public static Map<String, RequestUrlData> getUrlRules() {
		return urlRules;
	}
	// FIXME no implement
	private boolean validateHeaders(RequestMapping requestMapping)
			throws ServletException {
		// String[] headers = requestMapping.headers();
		return true;
	}
	public static boolean registMapping(String path,RequestUrlData data) {
		return urlRules.put(path, data)!=null;
	}
	public static RequestUrlData removeMapping(String path) {
		return urlRules.remove(path);
	}
	/** 
     * 
     * <p>matcher</p>
     * <p>Description:类路径与方法路径拼接匹配 </p>
     * <p> Copyright: 5just 2008-4-21</p>
     * <p> Company: linuxsir.org </p>
     * @author srchen
     * @date 2008-4-21 9:52:28 
     * @version 1.0
     * @return 
     */
//	private void matcher(Class c, String classUrlPrefix,
//			String classUrlPrefixParam) throws ServletException {
//		Method[] methods = c.getDeclaredMethods();
//		for (Method m : methods) {
//			RequestMapping methodMapping = m
//					.getAnnotation(RequestMapping.class);
//			if (methodMapping != null) {
//				String[] methodUrls = methodMapping.value();
//				String[] pathParamnames = methodMapping.pathParamnames();
//				if (methodUrls != null) {
//					boolean setPathParam = false;
//					if (pathParamnames != null
//							&& methodUrls.length == pathParamnames.length) {
//						setPathParam = true;
//					}
//					int i = 0;
//					for (String methodUrl : methodUrls) {
//						// if(urls.get(url)!)
//						String key = methodUrl;
//						if (classUrlPrefix != null)
//							key = classUrlPrefix + key;
//						Pattern p = Pattern.compile(key+"$");
//						RequestUrlData rd = new RequestUrlData();
//						rd.setMethod(m);
//						rd.setType(c);
//						rd.setRequestMapping(methodMapping);
//						if (classUrlPrefixParam != null) {
//							if (setPathParam) {
//								rd.setPathParamNames(classUrlPrefixParam + ","
//										+ pathParamnames[i++]);
//							} else {
//								rd.setPathParamNames(classUrlPrefixParam);
//							}
//						} else {
//							if (setPathParam) {
//								rd.setPathParamNames(pathParamnames[i++]);
//							}
//						}
//						urlRules.put(p, rd);
//					}
//				}
//			}
//		}
//
//	}

}
