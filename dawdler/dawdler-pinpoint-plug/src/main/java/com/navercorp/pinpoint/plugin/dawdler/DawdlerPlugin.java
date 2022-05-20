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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.dawdler.interceptor.DawdlerClientInterceptor;
import com.navercorp.pinpoint.plugin.dawdler.interceptor.DawdlerServerInterceptor;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerPlugin.java
 * @Description pinpoint插件常量类
 * @date 2021年4月3日
 * @email suxuan696@gmail.com
 */
public class DawdlerPlugin implements ProfilerPlugin, TransformTemplateAware {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

	private TransformTemplate transformTemplate;

	@Override
	public void setup(ProfilerPluginSetupContext context) {
		DawdlerConfiguration config = new DawdlerConfiguration(context.getConfig());
		if (!config.isDawdlerEnabled()) {
			logger.info("{} disabled", this.getClass().getSimpleName());
			return;
		}
		if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
			final DawdlerProviderDetector dawdlerProviderDetector = new DawdlerProviderDetector(
					config.getDawdlerBootstrapMains());
			if (dawdlerProviderDetector.detect()) {
				logger.info("Detected application type : {}", DawdlerConstants.DAWDLER_PROVIDER_SERVICE_TYPE);
				if (!context.registerApplicationType(DawdlerConstants.DAWDLER_PROVIDER_SERVICE_TYPE)) {
					logger.info("Application type [{}] already set, skipping [{}] registration.",
							context.getApplicationType(), DawdlerConstants.DAWDLER_PROVIDER_SERVICE_TYPE);
				}
			}
		}

		logger.info("Adding Dawdler transformers");
		this.addTransformers();
	}

	private void addTransformers() {
		transformTemplate.transform("com.anywide.dawdler.client.filter.DefaultFilterChain", ClientTransform.class);
		transformTemplate.transform("com.anywide.dawdler.server.filter.DefaultFilterChain", ServerTransform.class);
	}

	public static class ClientTransform implements TransformCallback {
		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws InstrumentException {
			final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
			InstrumentMethod invokeMethod = target.getDeclaredMethod("doFilter",
					"com.anywide.dawdler.core.bean.RequestBean");
			if (invokeMethod != null) {
				invokeMethod.addInterceptor(DawdlerClientInterceptor.class);
			}
			return target.toBytecode();
		}
	}

	public static class ServerTransform implements TransformCallback {
		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
				Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws InstrumentException {
			final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
			InstrumentMethod invokeMethod = target.getDeclaredMethod("doFilter",
					"com.anywide.dawdler.core.bean.RequestBean", "com.anywide.dawdler.core.bean.ResponseBean");
			if (invokeMethod != null) {
				invokeMethod.addInterceptor(DawdlerServerInterceptor.class);
			}
			return target.toBytecode();
		}
	}

	@Override
	public void setTransformTemplate(TransformTemplate transformTemplate) {
		this.transformTemplate = transformTemplate;
	}
}
