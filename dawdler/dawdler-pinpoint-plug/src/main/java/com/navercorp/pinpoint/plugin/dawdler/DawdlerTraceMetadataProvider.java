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

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author jackson.song
 * @version V1.0
 * pinpoint插件Metadata提供者,初始化配置
 */
public class DawdlerTraceMetadataProvider implements TraceMetadataProvider {
	@Override
	public void setup(TraceMetadataSetupContext context) {
		context.addServiceType(DawdlerConstants.DAWDLER_PROVIDER_SERVICE_TYPE);
		context.addServiceType(DawdlerConstants.DAWDLER_CONSUMER_SERVICE_TYPE);
		context.addServiceType(DawdlerConstants.DAWDLER_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
		context.addAnnotationKey(DawdlerConstants.DAWDLER_ARGS_ANNOTATION_KEY);
		context.addAnnotationKey(DawdlerConstants.DAWDLER_RESULT_ANNOTATION_KEY);
		context.addAnnotationKey(DawdlerConstants.DAWDLER_RPC_ANNOTATION_KEY);
	}
}
