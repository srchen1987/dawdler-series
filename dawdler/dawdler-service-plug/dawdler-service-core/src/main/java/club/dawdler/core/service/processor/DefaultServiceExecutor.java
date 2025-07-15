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
package club.dawdler.core.service.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.bean.RequestBean;
import club.dawdler.core.bean.ResponseBean;
import club.dawdler.core.exception.DawdlerOperateException;
import club.dawdler.core.service.bean.ServicesBean;
import club.dawdler.util.ReflectionUtil;
import club.dawdler.util.reflectasm.MethodAccess;

/**
 * @author jackson.song
 * @version V1.0
 * 默认服务处理器，不包含事务
 */
public class DefaultServiceExecutor implements ServiceExecutor {
	private static final Logger logger = LoggerFactory.getLogger(DefaultServiceExecutor.class);

	@Override
	public void execute(RequestBean requestBean, ResponseBean responseBean, ServicesBean servicesBean) {
		try {
			Object object = servicesBean.getService();
			String methodName = requestBean.getMethodName();
			MethodAccess methodAccess = ReflectionUtil.getMethodAccess(object);
			boolean fuzzy = requestBean.isFuzzy();
			int methodIndex;
			if (fuzzy) {
				methodIndex = methodAccess.getIndex(methodName,
						requestBean.getArgs() == null ? 0 : requestBean.getArgs().length);
			} else {
				methodIndex = methodAccess.getIndex(methodName, requestBean.getTypes());
			}
			object = ReflectionUtil.invoke(methodAccess, object, methodIndex, requestBean.getArgs());
			responseBean.setTarget(object);
		} catch (Throwable e) {
			responseBean.setCause(new DawdlerOperateException(new RuntimeException(e.toString())));
			logger.error("", e);
		}
	}
}
