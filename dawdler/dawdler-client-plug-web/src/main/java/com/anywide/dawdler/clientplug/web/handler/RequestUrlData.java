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

import com.anywide.dawdler.clientplug.web.annotation.JsonIgnoreNull;
import com.anywide.dawdler.clientplug.web.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.web.annotation.ResponseBody;

/**
 * @author jackson.song
 * @version V1.0
 * 一个包装的实体类
 */
public class RequestUrlData {
	private RequestMapping requestMapping;
	private Object target;
	private Method method;
	private ResponseBody responseBody;
	private JsonIgnoreNull jsonIgnoreNull;

	public RequestMapping getRequestMapping() {
		return requestMapping;
	}

	public void setRequestMapping(RequestMapping requestMapping) {
		this.requestMapping = requestMapping;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public ResponseBody getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(ResponseBody responseBody) {
		this.responseBody = responseBody;
	}

	public JsonIgnoreNull getJsonIgnoreNull() {
		return jsonIgnoreNull;
	}

	public void setJsonIgnoreNull(JsonIgnoreNull jsonIgnoreNull) {
		this.jsonIgnoreNull = jsonIgnoreNull;
	}

}
