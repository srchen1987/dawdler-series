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
package club.dawdler.clientplug.web.bind.resolver.impl;

import java.util.List;

import club.dawdler.clientplug.web.bind.param.RequestParamFieldData;
import club.dawdler.clientplug.web.handler.ViewForward;
import club.dawdler.clientplug.web.upload.UploadFile;

/**
 * @author jackson.song
 * @version V1.0
 * 获取文件上传参数值的决策者
 */
public class ServletUploadMethodArgumentResolver extends AbstractMethodArgumentResolver {

	@Override
	public boolean isSupport(RequestParamFieldData requestParamFieldData) {
		Class<?> type = requestParamFieldData.getType();
		return (UploadFile.class == type || UploadFile[].class == type);
	}

	@Override
	public Object resolveArgument(RequestParamFieldData requestParamFieldData, ViewForward viewForward, String uri) {
		Class<?> type = requestParamFieldData.getType();
		String paramName = getParameterName(requestParamFieldData);
		if (UploadFile.class == type) {
			return viewForward.paramFile(paramName);
		} else if (UploadFile[].class == type) {
			List<UploadFile> files = viewForward.paramFiles(paramName);
			if (files != null) {
				return files.toArray(new UploadFile[0]);
			}
			return null;
		}
		return null;
	}

}
