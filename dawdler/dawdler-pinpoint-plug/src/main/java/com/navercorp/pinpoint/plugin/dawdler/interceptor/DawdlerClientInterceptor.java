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
package com.navercorp.pinpoint.plugin.dawdler.interceptor;

import com.anywide.dawdler.client.filter.RequestWrapper;
import com.anywide.dawdler.core.rpc.context.RpcContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.dawdler.DawdlerConstants;

/**
 * @author jackson.song
 * @version V1.0
 * 基于pinpoint实现客户端拦截器
 */
public class DawdlerClientInterceptor implements AroundInterceptor {
	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private final boolean isDebug = logger.isDebugEnabled();

	private final MethodDescriptor descriptor;
	private final TraceContext traceContext;

	public DawdlerClientInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		this.traceContext = traceContext;
	}

	@Override
	public void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeInterceptor(target, args);
		}

		final Trace trace = traceContext.currentRawTraceObject();
		if (trace == null) {
			return;
		}

		final RequestWrapper request = (RequestWrapper) args[0];
		if (trace.canSampled()) {
			final SpanEventRecorder recorder = trace.traceBlockBegin();

			recorder.recordServiceType(DawdlerConstants.DAWDLER_CONSUMER_SERVICE_TYPE);

			final TraceId nextId = trace.getTraceId().getNextTraceId();

			recorder.recordNextSpanId(nextId.getSpanId());

			setAttachment(DawdlerConstants.META_TRANSACTION_ID, nextId.getTransactionId());
			setAttachment(DawdlerConstants.META_SPAN_ID, nextId.getSpanId());
			setAttachment(DawdlerConstants.META_PARENT_SPAN_ID, nextId.getParentSpanId());
			setAttachment(DawdlerConstants.META_PARENT_APPLICATION_TYPE, traceContext.getServerTypeCode());
			setAttachment(DawdlerConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
			setAttachment(DawdlerConstants.META_FLAGS, nextId.getFlags());
			setAttachment(DawdlerConstants.META_HOST, request.getRemoteAddress().toString());
		} else {
			setAttachment(DawdlerConstants.META_DO_NOT_TRACE, "1");
		}
	}

	private void setAttachment(String name, Object value) {
		RpcContext.getContext().setAttachment(name, value);
		if (isDebug) {
			logger.debug("Set attachment {}={}", name, value);
		}
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (isDebug) {
			logger.afterInterceptor(target, args);
		}

		final Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

		try {
			final RequestWrapper request = (RequestWrapper) args[0];
			final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
			recorder.recordApi(descriptor);
			if (throwable == null) {
				String endPoint = request.getRemoteAddress().toString();
				recorder.recordEndPoint(endPoint);
				recorder.recordDestinationId(endPoint);
				recorder.recordAttribute(DawdlerConstants.DAWDLER_ARGS_ANNOTATION_KEY, request.getArgs());
				recorder.recordAttribute(DawdlerConstants.DAWDLER_RESULT_ANNOTATION_KEY, result);
			} else {
				recorder.recordException(throwable);
			}
		} finally {
			trace.traceBlockEnd();
		}
	}

}
