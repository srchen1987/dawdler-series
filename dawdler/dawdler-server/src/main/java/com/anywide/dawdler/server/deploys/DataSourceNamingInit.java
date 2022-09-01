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
package com.anywide.dawdler.server.deploys;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.naming.ContextBindings;
import org.apache.naming.SelectorContext;
import org.apache.naming.java.javaURLContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.server.conf.DataSourceParser;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DataSourceNamingInit.java
 * @Description 服务器端数据源初始化
 * @date 2015年3月6日
 * @email suxuan696@gmail.com
 */
public class DataSourceNamingInit {
	private static final Logger logger = LoggerFactory.getLogger(DataSourceNamingInit.class);

	public static void init(ClassLoader classLoader)
			throws ClassNotFoundException, NamingException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Object token = new Object();
		Hashtable<String, Object> table = new Hashtable<>();
		SelectorContext selectorContext = new SelectorContext(table, true);
		ContextBindings.bindContext(classLoader, selectorContext, token);
		ContextBindings.bindClassLoader(classLoader, token, classLoader);
		System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
				"org.apache.naming.java.javaURLContextFactory");
		javaURLContextFactory jf = new javaURLContextFactory();
		Context context = jf.getInitialContext(table);
		Map<String, DataSource> datasource = DataSourceParser.getDataSource(null, classLoader);
		if (datasource != null) {
			datasource.forEach((K, V) -> {
				try {
					context.bind("java:" + K, V);
				} catch (NamingException e) {
					logger.error("", e);
				}
			});
		}
	}

}
