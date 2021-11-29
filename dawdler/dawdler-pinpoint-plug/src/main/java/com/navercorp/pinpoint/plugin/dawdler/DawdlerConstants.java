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

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerConstants.java
 * @Description pinpoint插件常量类
 * @date 2021年4月03日
 * @email suxuan696@gmail.com
 */
public final class DawdlerConstants {
	private DawdlerConstants() {
	}

	public static final ServiceType DAWDLER_PROVIDER_SERVICE_TYPE = ServiceTypeFactory.of(1919, "DAWDLER_PROVIDER",
			RECORD_STATISTICS);
	public static final ServiceType DAWDLER_CONSUMER_SERVICE_TYPE = ServiceTypeFactory.of(9917, "DAWDLER_CONSUMER",
			RECORD_STATISTICS);
	public static final ServiceType DAWDLER_PROVIDER_SERVICE_NO_STATISTICS_TYPE = ServiceTypeFactory.of(9919,
			"DAWDLER");
	public static final AnnotationKey DAWDLER_ARGS_ANNOTATION_KEY = AnnotationKeyFactory.of(917, "dawdler.args",
			VIEW_IN_RECORD_SET);
	public static final AnnotationKey DAWDLER_RESULT_ANNOTATION_KEY = AnnotationKeyFactory.of(918, "dawdler.result",
			VIEW_IN_RECORD_SET);
	public static final AnnotationKey DAWDLER_RPC_ANNOTATION_KEY = AnnotationKeyFactory.of(919, "dawdler.rpc",
			VIEW_IN_RECORD_SET);

	public static final String META_DO_NOT_TRACE = "_DAWDLER_DO_NOT_TRACE";
	public static final String META_TRANSACTION_ID = "_DAWDLER_TRASACTION_ID";
	public static final String META_SPAN_ID = "_DAWDLER_SPAN_ID";
	public static final String META_PARENT_SPAN_ID = "_DAWDLER_PARENT_SPAN_ID";
	public static final String META_PARENT_APPLICATION_NAME = "_DAWDLER_PARENT_APPLICATION_NAME";
	public static final String META_PARENT_APPLICATION_TYPE = "_DAWDLER_PARENT_APPLICATION_TYPE";
	public static final String META_FLAGS = "_DAWDLER_FLAGS";
	public static final String META_HOST = "_DAWDLER_HOST";

}
