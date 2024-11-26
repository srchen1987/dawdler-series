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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.web.annotation.RequestMapping.ViewType;
import com.anywide.dawdler.clientplug.web.plugs.PlugFactory;
import com.anywide.dawdler.clientplug.web.validator.ValidateParser;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlField;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator;
import com.anywide.dawdler.clientplug.web.validator.webbind.ValidateResourceLoader;
import com.anywide.dawdler.util.JsonProcessUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 对web请求进行校验
 */
public class WebValidateExecutor {
	private WebValidateExecutor() {
	}

	public static final String VALIDATE_ERROR = "validate_error";
	private static final Logger logger = LoggerFactory.getLogger(WebValidateExecutor.class);
	private static final Map<Class<?>, ControlValidator> VALIDATORS = new ConcurrentHashMap<>();

	public static ControlValidator loadControlValidator(Class<?> controllerClass) {
		ControlValidator cv = VALIDATORS.get(controllerClass);
		if (cv == null) {
			cv = ValidateResourceLoader.getControlValidator(controllerClass);
			if (cv == null) {
				cv = new ControlValidator();
			}
			ControlValidator preCv = VALIDATORS.putIfAbsent(controllerClass, cv);
			if (preCv != null) {
				cv = preCv;
			}
		}
		return cv;
	}

	public static boolean validate(HttpServletRequest request, HttpServletResponse response, Object controller,
			ViewForward viewForward) throws IOException {
		RequestMapping requestMapping = viewForward.getRequestMapping();
		Map<String, String> errors = new HashMap<>();
		Class<?> clazz = controller.getClass();
		ControlValidator cv = loadControlValidator(clazz);
		String uri = null;
		String antPath = viewForward.getAntPath();
		if (antPath != null) {
			uri = antPath;
		} else {
			uri = viewForward.getUriShort();
		}

		Map<String, ControlField> rules = cv.getParamFields(uri);
		if (rules != null) {
			if (requestMapping != null && requestMapping.generateValidator() && !rules.isEmpty()) {
				StringBuilder sb = new StringBuilder("sir_validate.addRule(");
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
				sb.append("sir_validator.buildFormValidate($formId);");
				System.out.println("######################################");
				System.out.println(sb.toString());
				System.out.println("######################################");
			}
			Map<String, String[]> params = viewForward.paramMaps();
			Set<Entry<String, ControlField>> rulesSet = rules.entrySet();
			for (Entry<String, ControlField> entry : rulesSet) {
				String key = entry.getKey();
				ControlField cf = entry.getValue();
				String error = ValidateParser.validate(cf.getFieldExplain(),
						params != null ? params.get(cf.getFieldName()) : null, cf.getRules());
				if (error != null) {
					errors.put(key, error);
				}
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
				PlugFactory.getDisplayPlug(ViewType.json.toString()).display(viewForward);
			}
			return false;
		}
		return true;
	}

	public static ControlValidator getControlValidator(Class<?> controllerClass) {
		return loadControlValidator(controllerClass);
	}

}
