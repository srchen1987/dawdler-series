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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dom4j.Element;
import org.dom4j.Node;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.serverplug.db.transaction.LocalConnectionFactory;
import com.anywide.dawdler.util.ReflectionUtil;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.spring.antpath.AntPathMatcher;

/**
 * @author jackson.song
 * @version V1.0
 * @Title RWSplittingDataSourceManager.java
 * @Description 读写分离的数据管理器
 * @date 2007年8月06日
 * @email suxuan696@gmail.com
 */
public class RWSplittingDataSourceManager {
	public static final String DATASOURCE_MANAGER_PREFIX = "DATASOURCE_MANAGER_PREFIX";
	private static final Pattern EXPRESSION = Pattern
			.compile("write=\\[(\\w+|(\\w+\\|\\w+)+)\\],read=\\[(\\w+|(\\w+\\|\\w+)+)\\]");
	private final Map<String, DataSource> dataSources = new HashMap<>();
	private final Map<String, String> dataourceExpression = new HashMap<>();
	private final Map<String, MappingDecision> packages = new HashMap<>();
	private final Map<String, MappingDecision> packagesAntPath = new LinkedHashMap<>();
	private DawdlerContext dawdlerContext;

	public RWSplittingDataSourceManager(DawdlerContext dawdlerContext) throws Exception {
		this.dawdlerContext = dawdlerContext;
		init();
	}

	public void init() throws Exception {
		XmlObject xmlo = dawdlerContext.getServicesConfig();
		List<Node> dataSources = xmlo.selectNodes("/config/datasources/datasource");
		for (Object dataSource : dataSources) {
			Element ele = (Element) dataSource;
			String id = ele.attributeValue("id");
			String code = ele.attributeValue("code");
			Class<?> clazz = Class.forName(code);
			Object obj = clazz.getDeclaredConstructor().newInstance();
			List<Node> attrs = ele.selectNodes("attribute");
			for (Node node : attrs) {
				Element e = (Element) node;
				String attributeName = e.attributeValue("name");
				String value = e.getText().trim();
				try {
					attributeName = captureName(attributeName);
					ReflectionUtil.invoke(obj, "set" + attributeName, Integer.parseInt(value));
				} catch (Exception ex) {
					ReflectionUtil.invoke(obj, "set" + attributeName, value);
				}

			}
			DataSource ds = (DataSource) obj;
			this.dataSources.put(id, ds);
		}

		List<Node> dataourceExpressionList = xmlo.selectNodes("/config/datasource-expressions/datasource-expression");
		for (Object dataourceExpression : dataourceExpressionList) {
			Element ele = (Element) dataourceExpression;
			String id = ele.attributeValue("id");
			String latentExpression = ele.attributeValue("latent-expression");
			this.dataourceExpression.put(id, latentExpression);
		}

		List<Node> decisionList = xmlo.selectNodes("/config/decisions/decision");
		for (Object decision : decisionList) {
			Element ele = (Element) decision;
			String mapping = ele.attributeValue("mapping");
			String latentExpression = ele.attributeValue("latent-expression");
			MappingDecision mappingDecision = new MappingDecision(dataourceExpression.get(latentExpression));
			if (dawdlerContext.getAntPathMatcher().isPattern(mapping)) {
				packagesAntPath.put(mapping, mappingDecision);
			} else {
				packages.put(mapping, mappingDecision);
			}

		}

	}

	public DataSource getDataSource(String id) {
		DataSource dataSource = dataSources.get(id);
		if (dataSource != null)
			return dataSource;
		try {
			dataSource = LocalConnectionFactory.getDataSourceInDawdler(id);
			dataSources.put(id, dataSource);
			return dataSource;
		} catch (NamingException e) {
			return null;
		}
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
			if (expression == null)
				return null;
			Matcher mc = EXPRESSION.matcher(expression);
			String[] rdstring = null;
			if (mc.matches()) {
				rdstring = new String[2];
				rdstring[0] = mc.group(1);
				rdstring[1] = mc.group(3);
				return rdstring;
			}
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof MappingDecision))
				return false;
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
