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
package club.dawdler.i18n;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import club.dawdler.i18n.annotation.I18nInjector;
import club.dawdler.util.TLS;
import club.dawdler.util.ResourceBundleUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 国际化操作类工厂
 */
public class I18nOperatorFactory {
	private static final Map<String, I18nOperator> I18N_OPERATORS = new ConcurrentHashMap<>();

	public static class I18nHandler implements InvocationHandler {
		private String baseName;

		public I18nHandler(String baseName) {
			this.baseName = baseName;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("get".equals(method.getName()) && args.length >= 1) {
				String key = (String) args[0];
				if (args.length == 1) {
					Locale locale = (Locale) TLS.get(ResourceBundleUtil.TLS_LOCALE);
					if (locale == null) {
						locale = Locale.getDefault();
					}
					ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
					return bundle.getString(key);
				} else if (args.length == 2) {
					Locale locale = (Locale) args[1];
					ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
					return bundle.getString(key);
				}
			}
			throw new UnsupportedOperationException("Method not supported: " + method.getName());
		}
	}

	private static Class<?>[] i18nOperatorClass = new Class[] { I18nOperator.class };

	public static I18nOperator getI18nOperator(String baseName) throws Exception {
		I18nOperator operator = I18N_OPERATORS.get(baseName);
		if (operator != null) {
			return operator;
		}
		synchronized (I18N_OPERATORS) {
			operator = I18N_OPERATORS.get(baseName);
			if (operator == null) {
				I18nHandler handler = new I18nHandler(baseName);
				operator = (I18nOperator) Proxy.newProxyInstance(I18nOperator.class.getClassLoader(),
						i18nOperatorClass, handler);
				I18N_OPERATORS.put(baseName, operator);
			}
		}
		return operator;
	}

	public static void initField(Object target, Class<?> serviceType) throws Throwable {
		Field[] fields = serviceType.getDeclaredFields();
		for (Field field : fields) {
			I18nInjector i18nInjector = field.getAnnotation(I18nInjector.class);
			if (!field.getType().isPrimitive()) {
				Class<?> serviceClass = field.getType();
				if (i18nInjector != null && I18nOperator.class.isAssignableFrom(serviceClass)) {
					field.setAccessible(true);
					field.set(target, getI18nOperator(i18nInjector.value()));
				}
			}
		}
	}
}