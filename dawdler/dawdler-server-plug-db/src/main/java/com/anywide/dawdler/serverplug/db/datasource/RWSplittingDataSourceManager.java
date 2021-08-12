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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dom4j.Element;
import org.dom4j.Node;

import com.anywide.dawdler.serverplug.db.transaction.LocalConnectionFacotry;
import com.anywide.dawdler.serverplug.util.XmlConfig;
import com.anywide.dawdler.util.ReflectionUtil;
import com.anywide.dawdler.util.XmlObject;

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
	private final Map<String, String> datasourceExpression = new HashMap<>();
	private final Map<String, MappingDecision> packages = new HashMap<>();

	public RWSplittingDataSourceManager() throws Exception {
		init();
	}

	public void init() throws Exception {
		XmlObject xmlo = XmlConfig.getConfig();
		List<Node> dataSources = xmlo.selectNodes("/config/datasources/datasource");
		for (Object dataSource : dataSources) {
			Element ele = (Element) dataSource;
			String id = ele.attributeValue("id");
			String code = ele.attributeValue("code");
			Class<?> c = Class.forName(code);
			Object obj = c.newInstance();
			List<Node> attrs = ele.selectNodes("attribute");
			for (Node node : attrs) {
				Element e = (Element) node;
				String attributeName = e.attributeValue("name");
				String value = e.getText().trim();
				try {
					ReflectionUtil.invoke(obj, "set" + attributeName, Integer.parseInt(value));
				} catch (Exception ex) {
					ReflectionUtil.invoke(obj, "set" + attributeName, value);
				}

			}
			DataSource ds = (DataSource) obj;
			this.dataSources.put(id, ds);
		}

		List<Node> datasourceExpressionList = xmlo.selectNodes("/config/datasource-expression");
		for (Object datasourceExpression : datasourceExpressionList) {
			Element ele = (Element) datasourceExpression;
			String id = ele.attributeValue("id");
			String latentExpression = ele.attributeValue("latent-expression");
			this.datasourceExpression.put(id, latentExpression);
		}

		List<Node> decisionList = xmlo.selectNodes("/config/decision");
		for (Object decision : decisionList) {
			Element ele = (Element) decision;
			String mapping = ele.attributeValue("mapping");
			String latentExpression = ele.attributeValue("latent-expression");
			packages.put(mapping, new MappingDecision(datasourceExpression.get(latentExpression)));
		}

	}

	public DataSource getDataSource(String id) {
		DataSource dataSource = dataSources.get(id);
		if (dataSource != null)
			return dataSource;
		try {
			dataSource = LocalConnectionFacotry.getDataSourceInDawdler(id);
			dataSources.put(id, dataSource);
			return dataSource;
		} catch (NamingException e) {
			return null;
		}
	}

	public MappingDecision getMappingDecision(String packageName) {
		return packages.get(packageName);
	}

	public class MappingDecision {
		private final AtomicInteger index = new AtomicInteger(0);
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

		public Connection[] getConnections() throws SQLException {
			int num = 0;
			if (wlength > 1 || rlength > 1)
				num = Math.abs(index.getAndIncrement());
			int position = 0;
			String write;
			if (wlength > 1) {
				position = num % wlength;
			}
			write = writeExpression[position];
			String read;
			int readPosition = 0;
			if (rlength > 1) {
				readPosition = num % rlength;
			}
			read = readExpression[readPosition];
			return new Connection[] { getDataSource(write).getConnection(), getDataSource(read).getConnection() };
		}

		public Connection getWriteConnection() throws SQLException {
			int num = 0;
			int position = 0;
			String write;
			if (wlength > 1) {
				num = Math.abs(index.getAndIncrement());
				position = num % wlength;
			}
			write = writeExpression[position];
			return getDataSource(write).getConnection();
		}

		public Connection getReadConnection() throws SQLException {
			int num = 0;
			int position = 0;
			String read;
			if (rlength > 1) {
				num = Math.abs(index.getAndIncrement());
				position = num % rlength;
			}
			read = readExpression[position];
			return getDataSource(read).getConnection();
		}

		public DataSource[] getDataSources() throws SQLException {
			int num = 0;
			if (wlength > 1 || rlength > 1)
				num = Math.abs(index.getAndIncrement());
			int position = 0;
			String write;
			if (wlength > 1) {
				position = num % wlength;
			}
			write = writeExpression[position];
			String read;
			int readPosition = 0;
			if (rlength > 1) {
				readPosition = num % rlength;
			}
			read = readExpression[readPosition];
			return new DataSource[] { getDataSource(write), getDataSource(read) };
		}

		public DataSource getWriteDataSource() {
			int num = 0;
			int position = 0;
			String write;
			if (wlength > 1) {
				num = Math.abs(index.getAndIncrement());
				position = num % wlength;
			}
			write = writeExpression[position];
			return getDataSource(write);
		}

		public DataSource getReadDataSource() {
			int num = 0;
			int position = 0;
			String read;
			if (rlength > 1) {
				num = Math.abs(index.getAndIncrement());
				position = num % rlength;
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
}
