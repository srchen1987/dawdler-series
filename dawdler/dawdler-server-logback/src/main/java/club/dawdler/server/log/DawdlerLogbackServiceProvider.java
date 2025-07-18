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
package club.dawdler.server.log;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LogbackServiceProvider;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * @author jackson.song
 * @version V1.0
 * logback扩展支持多模块
 */
public class DawdlerLogbackServiceProvider implements SLF4JServiceProvider {

	static final String NULL_CS_URL = CoreConstants.CODES_URL + "#null_CS";

	private IMarkerFactory markerFactory;
	private LogbackMDCAdapter mdcAdapter;

	private Map<ClassLoader, LoggerContext> instances = new HashMap<>();

	@Override
	public void initialize() {
		markerFactory = new BasicMarkerFactory();
		mdcAdapter = new LogbackMDCAdapter();
	}

	@Override
	public ILoggerFactory getLoggerFactory() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		LoggerContext context = instances.get(classLoader);
		if (context != null) {
			return context;
		}
		synchronized (instances) {
			context = instances.get(classLoader);
			if (context != null) {
				return context;
			}
			context = new LoggerContext();
			context.setName(CoreConstants.DEFAULT_CONTEXT_NAME + classLoader.getName());
			try {
				try {
					new DawdlerLogbackContextInitializer(context).autoConfig();
				} catch (JoranException je) {
					Util.report("Failed to auto configure default logger context", je);
				}
				if (!StatusUtil.contextHasStatusListener(context)) {
					StatusPrinter.printInCaseOfErrorsOrWarnings(context);
				}
			} catch (Exception t) { // see LOGBACK-1159
				Util.report("Failed to instantiate [" + LoggerContext.class.getName() + "]", t);
			}
			context.setMDCAdapter(mdcAdapter);
			context.start();
			instances.put(classLoader, context);
			return context;
		}

	}

	@Override
	public IMarkerFactory getMarkerFactory() {
		return markerFactory;
	}

	@Override
	public MDCAdapter getMDCAdapter() {
		return mdcAdapter;
	}

	@Override
	public String getRequestedApiVersion() {
		return LogbackServiceProvider.REQUESTED_API_VERSION;
	}

}
