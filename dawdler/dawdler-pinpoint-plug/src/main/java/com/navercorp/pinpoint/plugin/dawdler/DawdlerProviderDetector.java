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
package com.navercorp.pinpoint.plugin.dawdler;

import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.common.util.CollectionUtils;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerProviderDetector.java
 * @Description dawdler服务提供者pinpoint探测器
 * @date 2021年4月3日
 * @email suxuan696@gmail.com
 */
public final class DawdlerProviderDetector {
	private static final String DEFAULT_EXPECTED_MAIN_CLASS = "com.anywide.dawdler.server.bootstarp.Bootstrap";

	private List<String> expectedMainClasses;

	public DawdlerProviderDetector(List<String> expectedMainClasses) {
		if (CollectionUtils.isEmpty(expectedMainClasses)) {
			this.expectedMainClasses = Collections.singletonList(DEFAULT_EXPECTED_MAIN_CLASS);
		} else {
			this.expectedMainClasses = expectedMainClasses;
		}
	}

	public boolean detect() {
		String bootstrapMainClass = MainClassCondition.INSTANCE.getValue();
		boolean isExpectedMainClass = expectedMainClasses.contains(bootstrapMainClass);
		if (isExpectedMainClass) { 
			return true;
		}
		return false;
	}
}