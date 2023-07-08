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
package com.anywide.dawdler.serverplug.db.datasource;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.conf.ServicesConfig;
import com.anywide.dawdler.server.service.conf.ServicesConfig.DataSourceExpression;
import com.anywide.dawdler.server.service.conf.ServicesConfig.Decision;
import com.anywide.dawdler.serverplug.db.exception.DataSourceExpressionException;
import com.anywide.dawdler.util.ReflectionUtil;
import com.anywide.dawdler.util.spring.antpath.AntPathMatcher;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RWSplittingDataSourceManager.java
 * @Description 读写分离的数据管理器
 * @date 2007年8月6日
 * @email suxuan696@gmail.com
 */
public class RWSplittingDataSourceManager {
	public static final String DATASOURCE_MANAGER_PREFIX = "DATASOURCE_MANAGER_PREFIX";
	private static final Pattern EXPRESSION = Pattern.compile("write=\\[(.+)\\],read=\\[(.+)\\]");
	private final Map<String, DataSource> dataSources = new HashMap<>();
	private final Map<String, String> dataSourceExpression = new HashMap<>();
	private final Map<String, MappingDecision> packages = new HashMap<>();
	private final Map<String, MappingDecision> packagesAntPath = new LinkedHashMap<>();
	private DawdlerContext dawdlerContext;
	private static boolean useConfig = false;
	static {
		try {
			Class.forName("com.anywide.dawdler.conf.cache.ConfigMappingDataCache");
			useConfig = true;
		} catch (ClassNotFoundException e) {
		}
	}

	public RWSplittingDataSourceManager(DawdlerContext dawdlerContext) throws Exception {
		this.dawdlerContext = dawdlerContext;
		init();
	}

	public Map<String, DataSource> getDataSources() {
		return dataSources;
	}

	public void init() throws Exception {
		ServicesConfig servicesConfig = dawdlerContext.getServicesConfig();
		Map<String, Map<String, Object>> dataSources = servicesConfig.getDataSources();

		dataSources.forEach((id, attributes) -> {
			try {
				initDataSources(id, attributes);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});

		List<DataSourceExpression> dataSourceExpressionList = servicesConfig.getDataSourceExpressions();
		if (dataSourceExpressionList != null) {
			for (DataSourceExpression dataourceExpression : dataSourceExpressionList) {
				String id = dataourceExpression.getId();
				String latentExpression = dataourceExpression.getLatentExpression();
				this.dataSourceExpression.put(id, latentExpression);
			}
		}

		List<Decision> decisionList = servicesConfig.getDecisions();
		if (decisionList != null) {
			for (Decision decision : decisionList) {
				String mapping = decision.getMapping();
				String latentExpressionId = decision.getLatentExpressionId();
				MappingDecision mappingDecision = new MappingDecision(dataSourceExpression.get(latentExpressionId));
				if (dawdlerContext.getAntPathMatcher().isPattern(mapping)) {
					packagesAntPath.put(mapping, mappingDecision);
				} else {
					packages.put(mapping, mappingDecision);
				}
				if (mappingDecision.readExpression == null || mappingDecision.writeExpression == null) {
					throw new DataSourceExpressionException(latentExpressionId + ":"
							+ dataSourceExpression.get(latentExpressionId)
							+ " can't be null or must conform to write=[writeDataSource],read=[readDataSource1|readDataSource2] !");
				}
				if (useConfig) {
					for (String readDataSource : mappingDecision.readExpression) {
						initDataSourcesFromConfigServer(readDataSource);
					}
					for (String writeDataSource : mappingDecision.writeExpression) {
						initDataSourcesFromConfigServer(writeDataSource);
					}
				}
			}
		}

	}

	private void initDataSources(String id, Map<String, Object> attributes)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		if (dataSources.containsKey(id)) {
			return;
		}
		String type = (String) attributes.get("type");
		if (type == null) {
			throw new NullPointerException("dataSource attribute [type] can't be null!");
		}
		Class<?> clazz = Class.forName(type);
		Object obj = clazz.getDeclaredConstructor().newInstance();

		attributes.forEach((k, v) -> {
			if (k.equals("type")) {
				return;
			}
			String attributeName = captureName(k);
			try {
				ReflectionUtil.invoke(obj, "set" + attributeName, v.toString());
			} catch (Exception ex) {
				try {
					ReflectionUtil.invoke(obj, "set" + attributeName, Integer.parseInt(v.toString()));
				} catch (Exception exception) {
					ReflectionUtil.invoke(obj, "set" + attributeName, Long.parseLong(v.toString()));
				}
			}
		});

		DataSource ds = (DataSource) obj;
		dataSources.put(id, ds);
	}

	public void initDataSourcesFromConfigServer(String id) throws Exception {
		Map<String, Object> attributes = com.anywide.dawdler.conf.cache.ConfigMappingDataCache.getMappingDataCache(id);
		if (attributes != null) {
			initDataSources(id, attributes);
		}
	}

	public DataSource getDataSource(String id) {
		return dataSources.get(id);
	}

	public MappingDecision getMappingDecision(String packageName) {
		MappingDecision mappingDecision = packages.get(packageName);
		if (mappingDecision == null) {
			Set<String> keys = packagesAntPath.keySet();
			AntPathMatcher antPathMatcher = dawdlerContext.getAntPathMatcher();
			for (String key : keys) {
				if (antPathMatcher.match(key, packageName)) {
					mappingDecision = packagesAntPath.get(key);
					packages.putIfAbsent(key, mappingDecision);// ignored concurrent access
					break;
				}
			}
		}
		return mappingDecision;
	}

	public class MappingDecision {
		private String[] readExpression;
		private int rlength;
		private String[] writeExpression;
		private int wlength;
		private String originalReadExpression;

		public MappingDecision(String latentExpression) {
			String[] expression = explainExpression(latentExpression);
			if (expression != null) {
				writeExpression = expression[0].split("\\|");
				wlength = writeExpression.length;
				originalReadExpression = expression[1];
				readExpression = expression[1].split("\\|");
				rlength = readExpression.length;
			}
		}

		public DataSource getWriteDataSource(long index) {
			int position = 0;
			String write;
			if (wlength > 1) {
				position = (int) (index % wlength);
			}
			write = writeExpression[position];
			return getDataSource(write);
		}

		public DataSource getReadDataSource(long index) {
			int position = 0;
			String read;
			if (rlength > 1) {
				position = (int) (index % rlength);
			}
			read = readExpression[position];
			return getDataSource(read);
		}

		public String[] explainExpression(String expression) {
			if (expression == null) {
				return null;
			}
			Matcher mc = EXPRESSION.matcher(expression);
			String[] rdstring = null;
			if (mc.matches()) {
				rdstring = new String[2];
				rdstring[0] = mc.group(1);
				rdstring[1] = mc.group(2);
				return rdstring;
			}
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof MappingDecision)) {
				return false;
			}
			MappingDecision md = (MappingDecision) obj;
			return originalReadExpression != null && (originalReadExpression.equals(md.originalReadExpression));
		}
	}

	private static String captureName(String str) {
		char[] cs = str.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);
	}

}
