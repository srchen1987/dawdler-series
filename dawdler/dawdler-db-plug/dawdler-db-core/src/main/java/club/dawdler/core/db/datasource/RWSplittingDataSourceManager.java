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
package club.dawdler.core.db.datasource;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import club.dawdler.core.db.conf.DataSourceExpression;
import club.dawdler.core.db.conf.DbConfig;
import club.dawdler.core.db.conf.Decision;
import club.dawdler.core.db.exception.DataSourceExpressionException;
import club.dawdler.util.PropertiesUtil;
import club.dawdler.util.ReflectionUtil;
import club.dawdler.util.spring.antpath.AntPathMatcher;

/**
 * @author jackson.song
 * @version V1.0
 * 读写分离的数据管理器
 */
public class RWSplittingDataSourceManager {
	public static final String DATASOURCE_MANAGER_PREFIX = "DATASOURCE_MANAGER_PREFIX";
	private static final Pattern EXPRESSION = Pattern.compile("write=\\[(.+)\\],read=\\[(.+)\\]");
	private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
	private final Map<String, String> dataSourceExpression = new HashMap<>();
	private final Map<String, MappingDecision> packages = new HashMap<>(128);
	private final Map<String, MappingDecision> packagesAntPath = new LinkedHashMap<>();
	private final DbConfig dbConfig;
	private static final AntPathMatcher antPathMatcher = AntPathMatcher.DEFAULT_INSTANCE;
	private static RWSplittingDataSourceManager instance;

	public static final RWSplittingDataSourceManager getInstance() {
		return instance;
	}

	public static final void init(DbConfig dbConfig) throws Exception {
		instance = new RWSplittingDataSourceManager(dbConfig);
	}

	public RWSplittingDataSourceManager(DbConfig dbConfig) throws Exception {
		this.dbConfig = dbConfig;
		init();
	}

	public Map<String, DataSource> getDataSources() {
		return dataSources;
	}

	public void init() throws Exception {
		List<DataSourceExpression> dataSourceExpressionList = dbConfig.getDataSourceExpressions();
		if (dataSourceExpressionList != null) {
			for (DataSourceExpression dataSourceExpression : dataSourceExpressionList) {
				String id = dataSourceExpression.getId();
				String latentExpression = dataSourceExpression.getLatentExpression();
				this.dataSourceExpression.put(id, latentExpression);
			}
		}

		List<Decision> decisionList = dbConfig.getDecisions();
		if (decisionList != null) {
			for (Decision decision : decisionList) {
				String mapping = decision.getMapping();
				String latentExpressionId = decision.getLatentExpressionId();
				MappingDecision mappingDecision = new MappingDecision(dataSourceExpression.get(latentExpressionId));
				if (antPathMatcher.isPattern(mapping)) {
					packagesAntPath.put(mapping, mappingDecision);
				} else {
					packages.put(mapping, mappingDecision);
				}
				if (mappingDecision.readExpression == null || mappingDecision.writeExpression == null) {
					throw new DataSourceExpressionException(latentExpressionId + ":"
							+ dataSourceExpression.get(latentExpressionId)
							+ " can't be null or must conform to write=[writeDataSource],read=[readDataSource1|readDataSource2] !");
				}
				for (String readDataSource : mappingDecision.readExpression) {
					initDataSourcesFromPropertiesIfNotExistLoadConfigCenter(readDataSource);
				}
				for (String writeDataSource : mappingDecision.writeExpression) {
					initDataSourcesFromPropertiesIfNotExistLoadConfigCenter(writeDataSource);
				}
			}
		}
	}

	private void initDataSources(String id, Map<String, Object> attributes)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		String type = (String) attributes.get("type");
		if (type == null) {
			throw new NullPointerException("dataSource attribute [type] can't be null!");
		}
		Class<?> clazz = Class.forName(type);
		Object obj = clazz.getDeclaredConstructor().newInstance();

		attributes.forEach((k, v) -> {
			if (v == null) {
				v = "";
			}
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
					try {
						ReflectionUtil.invoke(obj, "set" + attributeName, Long.parseLong(v.toString()));
					} catch (Exception e) {
						try {
							ReflectionUtil.invoke(obj, "set" + attributeName, Boolean.parseBoolean(v.toString()));
						} catch (Exception ec) {
							throw new RuntimeException("not found " + attributeName);
						}
					}
				}
			}
		});
		DataSource ds = (DataSource) obj;
		dataSources.put(id, ds);
	}

	public void initDataSourcesFromPropertiesIfNotExistLoadConfigCenter(String id) throws Exception {
		if (dataSources.containsKey(id)) {
			return;
		}
		Properties ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter(id);
		if (ps != null) {
			Map<String, Object> attributes = new HashMap<>();
			ps.forEach((k, v) -> {
				attributes.put(k.toString(), v);
			});
			initDataSources(id, attributes);
		}
	}

	public DataSource getDataSource(String id) {
		DataSource dataSource = dataSources.get(id);
		if (dataSource == null) {
			try {
				synchronized (this) {
					dataSource = dataSources.get(id);
					if (dataSource == null) {
						initDataSourcesFromPropertiesIfNotExistLoadConfigCenter(id);
					}
				}
			} catch (Exception e) {
			}
			dataSource = dataSources.get(id);
			if (dataSource == null) {
				throw new RuntimeException("not found dataSource " + id + "!");
			}
		}
		return dataSource;
	}

	public MappingDecision getMappingDecision(String packageName) {
		MappingDecision mappingDecision = packages.get(packageName);
		if (mappingDecision == null) {
			Set<String> keys = packagesAntPath.keySet();
			for (String key : keys) {
				if (antPathMatcher.match(key, packageName)) {
					mappingDecision = packagesAntPath.get(key);
					packages.putIfAbsent(packageName, mappingDecision);// ignored concurrent access
					break;
				}
			}
		}
		return mappingDecision;
	}

	public class MappingDecision {
		private String[] readExpression;
		private int rLength;
		private String[] writeExpression;
		private int wLength;
		private String originalReadExpression;

		public MappingDecision(String latentExpression) {
			String[] expression = explainExpression(latentExpression);
			if (expression != null) {
				writeExpression = expression[0].split("\\|");
				wLength = writeExpression.length;
				originalReadExpression = expression[1];
				readExpression = expression[1].split("\\|");
				rLength = readExpression.length;
			}
		}

		public DataSource getWriteDataSource(String subfix, long index) {
			int position = 0;
			String write;
			if (wLength > 1) {
				position = (int) (index % wLength);
			}
			write = writeExpression[position];
			return getDataSource(subfix == null ? write : write.concat(subfix));
		}

		public DataSource getReadDataSource(String subfix, long index) {
			int position = 0;
			String read;
			if (rLength > 1) {
				position = (int) (index % rLength);
			}
			read = readExpression[position];
			return getDataSource(subfix == null ? read : read.concat(subfix));
		}

		public String[] explainExpression(String expression) {
			if (expression == null) {
				return null;
			}
			Matcher mc = EXPRESSION.matcher(expression);
			String[] rdString = null;
			if (mc.matches()) {
				rdString = new String[2];
				rdString[0] = mc.group(1);
				rdString[1] = mc.group(2);
				return rdString;
			}
			return null;
		}

		public boolean needBalance() {
			return rLength > 1 || wLength > 1;
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
