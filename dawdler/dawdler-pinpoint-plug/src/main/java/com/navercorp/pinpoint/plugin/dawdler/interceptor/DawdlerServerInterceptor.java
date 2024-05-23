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

import com.anywide.dawdler.core.bean.ResponseBean;
import com.anywide.dawdler.server.filter.RequestWrapper;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.dawdler.DawdlerConstants;
import com.navercorp.pinpoint.plugin.dawdler.DawdlerProviderMethodDescriptor;

/**
 * @author jackson.song
 * @version V1.0
 * 基于pinpoint实现服务端拦截器
 */
public class DawdlerServerInterceptor extends SpanRecursiveAroundInterceptor {
	private static final String SCOPE_NAME = "##DAWDLER_PROVIDER_TRACE";
	private static final MethodDescriptor DAWDLER_PROVIDER_METHOD_DESCRIPTOR = new DawdlerProviderMethodDescriptor();

	public DawdlerServerInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
		super(traceContext, descriptor, SCOPE_NAME);
		traceContext.cacheApi(DAWDLER_PROVIDER_METHOD_DESCRIPTOR);
	}

	@Override
	protected Trace createTrace(Object target, Object[] args) {
		final Trace trace = readRequestTrace(target, args);
		if (trace.canSampled()) {
			final SpanRecorder recorder = trace.getSpanRecorder();
			recorder.recordServiceType(DawdlerConstants.DAWDLER_PROVIDER_SERVICE_TYPE);
			recorder.recordApi(DAWDLER_PROVIDER_METHOD_DESCRIPTOR);
			recordRequest(recorder, target, args);
		}

		return trace;
	}

	private Trace readRequestTrace(Object target, Object[] args) {
		final RequestWrapper request = (RequestWrapper) args[0];
		if (request.getAttachment(DawdlerConstants.META_DO_NOT_TRACE) != null) {
			return traceContext.disableSampling();
		}
		final String transactionId = (String) request.getAttachment(DawdlerConstants.META_TRANSACTION_ID);
		if (transactionId == null) {
			return traceContext.newTraceObject();
		}

		final long parentSpanID = parseLong((Long) request.getAttachment(DawdlerConstants.META_PARENT_SPAN_ID),
				SpanId.NULL);
		final long spanID = parseLong((Long) request.getAttachment(DawdlerConstants.META_SPAN_ID), SpanId.NULL);
		final short flags = parseShort((Short) request.getAttachment(DawdlerConstants.META_FLAGS), (short) 0);
		final TraceId traceId = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);
		return traceContext.continueTraceObject(traceId);
	}

	private void recordRequest(SpanRecorder recorder, Object target, Object[] args) {
		final RequestWrapper request = (RequestWrapper) args[0];
		recorder.recordRpcName(request.getServiceName() + ":" + request.getMethodName());
		if (request.getRemoteAddress() != null) {
			recorder.recordRemoteAddress(request.getRemoteAddress().toString());
		} else {
			recorder.recordRemoteAddress("Unknown");
		}
		recorder.recordEndPoint(request.getLocalAddress().toString());

		if (!recorder.isRoot()) {
			final String parentApplicationName = (String) request
					.getAttachment(DawdlerConstants.META_PARENT_APPLICATION_NAME);
			if (parentApplicationName != null) {
				final short parentApplicationType = parseShort(
						(Short) request.getAttachment(DawdlerConstants.META_PARENT_APPLICATION_TYPE),
						ServiceType.UNDEFINED.getCode());
				recorder.recordParentApplication(parentApplicationName, parentApplicationType);
				final String host = (String) request.getAttachment(DawdlerConstants.META_HOST);
				if (host != null) {
					recorder.recordAcceptorHost(host);
				} else {
					final String estimatedLocalHost = request.getLocalAddress().toString();
					if (estimatedLocalHost != null) {
						recorder.recordAcceptorHost(estimatedLocalHost);
					}
				}
			}
		}
	}

	@Override
	protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
		final RequestWrapper request = (RequestWrapper) args[0];
		recorder.recordServiceType(DawdlerConstants.DAWDLER_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
		recorder.recordApi(methodDescriptor);
		recorder.recordAttribute(DawdlerConstants.DAWDLER_RPC_ANNOTATION_KEY,
				request.getServiceName() + ":" + request.getMethodName() + classArraytoString(request.getTypes()));
	}

	public static String classArraytoString(Class<?>[] a) {
		if (a == null) {
			return "()";
		}
		int iMax = a.length - 1;
		StringBuilder b = new StringBuilder();
		b.append('(');
		for (int i = 0;; i++) {
			b.append(a[i].getName());
			if (i == iMax) {
				return b.append(')').toString();
			}
			b.append(", ");
		}
	}

	@Override
	protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
			Throwable throwable) {
		final RequestWrapper request = (RequestWrapper) args[0];
		final ResponseBean response = (ResponseBean) args[1];
		recorder.recordServiceType(DawdlerConstants.DAWDLER_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
		recorder.recordApi(methodDescriptor);
		if (request.getArgs() != null) {
			StringBuilder argsContent = new StringBuilder();
			for (Object obj : request.getArgs()) {
				argsContent.append(obj);
				argsContent.append(",");
			}
			argsContent.deleteCharAt(argsContent.length() - 1);
			recorder.recordAttribute(DawdlerConstants.DAWDLER_ARGS_ANNOTATION_KEY, argsContent);
		}
		if (response.getCause() == null) {
			recorder.recordAttribute(DawdlerConstants.DAWDLER_RESULT_ANNOTATION_KEY, response.getTarget());
		} else {
			recorder.recordException(response.getCause());
		}
	}

	public Long parseLong(Long value, Long defaultValue) {
		if (value != null) {
			return value;
		}
		return defaultValue;
	}

	public Short parseShort(Short value, Short defaultValue) {
		if (value != null) {
			return value;
		}
		return defaultValue;
	}

}