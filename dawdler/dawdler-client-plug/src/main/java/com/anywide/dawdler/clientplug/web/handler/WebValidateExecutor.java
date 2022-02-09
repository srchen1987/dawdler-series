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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.plugs.PlugFactory;
import com.anywide.dawdler.clientplug.web.validator.ValidateParser;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlField;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator;
import com.anywide.dawdler.clientplug.web.validator.webbind.ValidateResourceLoader;
import com.anywide.dawdler.clientplug.web.wrapper.BodyReaderHttpServletRequestWrapper;
import com.anywide.dawdler.util.JsonProcessUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebValidateExecutor.java
 * @Description 对web请求进行校验
 * @date 2007年7月23日
 * @email suxuan696@gmail.com
 */
public class WebValidateExecutor {
	public static final String VALIDATE_ERROR = "validate_error";// 验证错误
	private static final Logger logger = LoggerFactory.getLogger(WebValidateExecutor.class);
	private static final Map<Class<?>, ControlValidator> validators = new ConcurrentHashMap<>();

	public static boolean validate(HttpServletRequest request, HttpServletResponse response, boolean isJson,
			Object controller) throws IOException {
		if (!(controller instanceof TransactionController)) {
			return true;
		}
		ViewForward viewForward = ViewControllerContext.getViewForward();
		RequestMapping requestMapping = viewForward.getRequestMapping();
		Map<String, Serializable> errors = new HashMap<>();
		Class<?> clazz = controller.getClass();
		ControlValidator cv = validators.get(clazz);
		if (cv == null) {
			cv = ValidateResourceLoader.getControlValidator(clazz);
			if (cv == null)
				cv = new ControlValidator();
			ControlValidator preCv = validators.putIfAbsent(clazz, cv);
			if (preCv != null)
				cv = preCv;
		}
		String uri = viewForward.getUriShort();
		if (!cv.isValidate() || uri == null)
			return true;
		Map<String, ControlField> rules = cv.getMappings().get(uri);
		if (rules == null)
			rules = cv.getGlobalControlFields();
		if (rules != null) {
			if (requestMapping != null && requestMapping.generateValidator() && !rules.isEmpty()) {
				StringBuffer sb = new StringBuffer("sir_validate.addRule(");
				Collection<ControlField> cc = rules.values();
				List<Map<String, String>> list = new ArrayList<>();
				for (Iterator<ControlField> it = cc.iterator(); it.hasNext();) {
					Map<String, String> map = new LinkedHashMap<>();
					ControlField cf = it.next();
					map.put("id", cf.getFieldName());
					map.put("viewName", cf.getFieldExplain());
					map.put("validateRule", cf.getRules());
					list.add(map);
				}
				sb.append(JsonProcessUtil.beanToJson(list));
				sb.append(");\n");
				sb.append("sir_validate.buildFormValidate($formid);");
				System.out.println("######################################");
				System.out.println(sb.toString());
				System.out.println("######################################");
			}
			Map params = null;
			if (isJson) {
				if (request.getClass() == BodyReaderHttpServletRequestWrapper.class) {
					BodyReaderHttpServletRequestWrapper requestWrapper = (BodyReaderHttpServletRequestWrapper) request;
					params = JsonProcessUtil.jsonToBean(requestWrapper.getBody(), HashMap.class);
				} else {
					params = JsonProcessUtil.jsonToBean(request.getInputStream(), HashMap.class);
				}
			} else {
				params = viewForward.paramMaps();
			}
			Set<Entry<String, ControlField>> rulesSet = rules.entrySet();
			for (Entry<String, ControlField> entry : rulesSet) {
				String key = entry.getKey();
				ControlField cf = entry.getValue();
				String error = ValidateParser.validate(cf.getFieldExplain(),
						params != null ? params.get(cf.getFieldName()) : null, cf.getRules());
				if (error != null)
					errors.put(key, error);
			}
		}
		if (!errors.isEmpty()) {
			if (requestMapping != null && requestMapping.input() != null && !requestMapping.input().trim().equals("")) {
				request.setAttribute(VALIDATE_ERROR, errors);
				try {
					request.getRequestDispatcher(requestMapping.input()).forward(request, response);
				} catch (ServletException | IOException e) {
					logger.error("", e);
				}
			} else {
				viewForward.putData(VALIDATE_ERROR, errors);
				PlugFactory.getDisplayPlug("json").display(viewForward);
			}
			return false;
		}
		return true;
	}

}
