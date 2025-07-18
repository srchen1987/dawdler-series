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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.util.ClassicEnvUtil;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

/**
 * @author jackson.song
 * @version V1.0
 * logback 上下文初始化
 */
public class DawdlerLogbackContextInitializer {

	public static final String AUTOCONFIG_FILE = "logback.xml";
	public static final String TEST_AUTOCONFIG_FILE = "logback-test.xml";

	final LoggerContext loggerContext;

	public DawdlerLogbackContextInitializer(LoggerContext loggerContext) {
		this.loggerContext = loggerContext;
	}

	public void configureByResource(URL url) throws JoranException {
		if (url == null) {
			throw new IllegalArgumentException("URL argument cannot be null");
		}
		final String urlString = url.toString();
		if (urlString.endsWith("xml")) {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			configurator.doConfigure(url);
		} else {
			throw new LogbackException(
					"Unexpected filename extension of file [" + url.toString() + "]. Should be .xml");
		}
	}

	void joranConfigureByResource(URL url) throws JoranException {
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		configurator.doConfigure(url);
	}

	private URL findConfigFileURLFromSystemProperties(ClassLoader classLoader, boolean updateStatus) {
		String logbackConfigFile = OptionHelper.getSystemProperty(ClassicConstants.CONFIG_FILE_PROPERTY);
		if (logbackConfigFile != null) {
			URL result = null;
			try {
				result = new URL(logbackConfigFile);
				return result;
			} catch (MalformedURLException e) {
				result = Loader.getResource(logbackConfigFile, classLoader);
				if (result != null) {
					return result;
				}
				File f = new File(logbackConfigFile);
				if (f.exists() && f.isFile()) {
					try {
						result = f.toURI().toURL();
						return result;
					} catch (MalformedURLException e1) {
					}
				}
			} finally {
				if (updateStatus) {
					statusOnResourceSearch(logbackConfigFile, result);
				}
			}
		}
		return null;
	}

	public URL findURLOfDefaultConfigurationFile(boolean updateStatus) {
		ClassLoader myClassLoader = Thread.currentThread().getContextClassLoader();
		URL url = findConfigFileURLFromSystemProperties(myClassLoader, updateStatus);
		if (url != null) {
			return url;
		}

		url = getResource(TEST_AUTOCONFIG_FILE, myClassLoader, updateStatus);
		if (url != null) {
			return url;
		}

		return getResource(AUTOCONFIG_FILE, myClassLoader, updateStatus);
	}

	private URL getResource(String filename, ClassLoader myClassLoader, boolean updateStatus) {
		URL url = Loader.getResource(filename, myClassLoader);
		if (updateStatus) {
			statusOnResourceSearch(filename, url);
		}
		return url;
	}

	public void autoConfig() throws JoranException {
		StatusListenerConfigHelper.installIfAsked(loggerContext);
		URL url = findURLOfDefaultConfigurationFile(true);
		if (url != null) {
			configureByResource(url);
		} else {
			List<Configurator> configurators = ClassicEnvUtil.loadFromServiceLoader(Configurator.class,
					Thread.currentThread().getContextClassLoader());
			if (configurators != null) {
				for (Configurator c : configurators) {
					try {
						c.setContext(loggerContext);
						c.configure(loggerContext);
					} catch (Exception e) {
						throw new LogbackException(
								String.format("Failed to initialize Configurator: %s using ServiceLoader",
										c != null ? c.getClass().getCanonicalName() : "null"),
								e);
					}
				}
			} else {
				BasicConfigurator basicConfigurator = new BasicConfigurator();
				basicConfigurator.setContext(loggerContext);
				basicConfigurator.configure(loggerContext);
			}
		}
	}

	private void statusOnResourceSearch(String resourceName, URL url) {
		StatusManager sm = loggerContext.getStatusManager();
		if (url == null) {
			sm.add(new InfoStatus("Could NOT find resource [" + resourceName + "]", loggerContext));
		} else {
			sm.add(new InfoStatus("Found resource [" + resourceName + "] at [" + url.toString() + "]", loggerContext));
		}
	}

}
