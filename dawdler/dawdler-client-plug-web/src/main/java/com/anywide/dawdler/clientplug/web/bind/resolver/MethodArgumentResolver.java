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
package com.anywide.dawdler.clientplug.web.bind.resolver;

import com.anywide.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;

/**
 * @author jackson.song
 * @version V1.0
 * @Title MethodArgumentResolver.java
 * @Description 获取参数值的决策接口
 * @date 2021年4月3日
 * @email suxuan696@gmail.com
 */
public interface MethodArgumentResolver {

	boolean isSupport(RequestParamFieldData requestParamFieldData);

	Object resolveArgument(RequestParamFieldData requestParamFieldData, ViewForward viewForward, String uri)
			throws Exception;
}
