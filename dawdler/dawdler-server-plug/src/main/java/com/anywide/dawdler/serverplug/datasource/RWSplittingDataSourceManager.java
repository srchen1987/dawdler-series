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
package com.anywide.dawdler.serverplug.datasource;
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
import com.anywide.dawdler.serverplug.transaction.LocalConnectionFacotry;
import com.anywide.dawdler.serverplug.util.XmlConfig;
import com.anywide.dawdler.util.ReflectionUtil;
import com.anywide.dawdler.util.XmlObject;
/**
 * 
 * @Title:  RWSplittingDataSourceManager.java   
 * @Description:    读写分离的数据管理器 
 * @author: jackson.song    
 * @date:   2007年08月06日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class RWSplittingDataSourceManager {
	public static final String DATASOURCEMANAGER_PREFIX = "DATASOURCEMANAGER_PREFIX";
	private Map<String, DataSource> datasources = new HashMap<>();
	private Map<String, String> datasourceExpression = new HashMap<>();
	private Map<String, MappingDecision> packages = new HashMap<>();
	// private static final Pattern EXPRESSION =
	// Pattern.compile("read=\\[(\\w+|\\$\\d+|(\\$\\d+\\|\\w+|\\w+\\|\\$\\d+|\\|\\$\\d+|\\|\\w+|\\$\\d+\\|\\$\\d+|\\w+\\|\\w+)+)\\],write=\\[(\\w+|\\$\\d+|(\\$\\d+\\|\\w+|\\w+\\|\\$\\d+|\\|\\$\\d+|\\|\\w+|\\$\\d+\\|\\$\\d+|\\w+\\|\\w+)+)\\]");
	private static final Pattern EXPRESSION = Pattern
			.compile("write=\\[(\\w+|(\\w+\\|\\w+)+)\\],read=\\[(\\w+|(\\w+\\|\\w+)+)\\]");

	public RWSplittingDataSourceManager() throws Exception {
		init();
	}

	public void init() throws Exception {
		XmlObject xmlo = XmlConfig.getConfig();
		List<Element> list = xmlo.getNode("/config/datasources/datasource");
		for (Element ele : list) {
			String id = ele.attributeValue("id");
			String code = ele.attributeValue("code");
			Class c = Class.forName(code);
			Object obj = c.newInstance();
			List<Element> attrs = ele.selectNodes("attribute");
			for (Element e : attrs) {
				String attributeName = e.attributeValue("name");
				String value = e.getText().trim();
				try {
					ReflectionUtil.invoke(obj, "set" + attributeName, Integer.parseInt(value));
				} catch (Exception ex) {
					ReflectionUtil.invoke(obj, "set" + attributeName, value);
				}

			}
			DataSource ds = (DataSource) obj;
			datasources.put(id, ds);
		}

		List<Element> datasourceExpressionList = xmlo.getNode("/config/datasource_expression");
		for (Element ele : datasourceExpressionList) {
			String id = ele.attributeValue("id");
			String latentExpression = ele.attributeValue("latent_expression");
			datasourceExpression.put(id, latentExpression);
		}

		List<Element> decisionList = xmlo.getNode("/config/decision");
		for (Element ele : decisionList) {
			String mapping = ele.attributeValue("mapping");
			String latentExpression = ele.attributeValue("latent_expression");
			packages.put(mapping, new MappingDecision(datasourceExpression.get(latentExpression)));
		}

	}

	public DataSource getDataSource(String id) {
		DataSource dataSource = datasources.get(id);
		if (dataSource != null)
			return dataSource;
		try {
			dataSource = LocalConnectionFacotry.getDataSourceInDawdler(id);
			datasources.put(id, dataSource);
			return dataSource;
		} catch (NamingException e) {
			return null;
		}
	}

	public MappingDecision getMappingDecision(String packageName) {
		return packages.get(packageName);
	}

	public class MappingDecision {
		private AtomicInteger index = new AtomicInteger(0);
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
			String wirte;
			if (wlength > 1) {
				position = num % wlength;
			}
			wirte = writeExpression[position];
			String read;
			int readPosition = 0;
			if (rlength > 1) {
				readPosition = num % rlength;
			}
			read = readExpression[readPosition];
			return new Connection[] { getDataSource(wirte).getConnection(), getDataSource(read).getConnection() };
		}

		public Connection getWriteConnection() throws SQLException {
			int num = 0;
			int position = 0;
			String wirte;
			if (wlength > 1) {
				num = Math.abs(index.getAndIncrement());
				position = num % wlength;
			}
			wirte = writeExpression[position];
			return getDataSource(wirte).getConnection();
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
			String wirte;
			if (wlength > 1) {
				position = num % wlength;
			}
			wirte = writeExpression[position];
			String read;
			int readPosition = 0;
			if (rlength > 1) {
				readPosition = num % rlength;
			}
			read = readExpression[readPosition];
			return new DataSource[] { getDataSource(wirte), getDataSource(read) };
		}

		public DataSource getWriteDataSource() {
			int num = 0;
			int position = 0;
			String wirte;
			if (wlength > 1) {
				num = Math.abs(index.getAndIncrement());
				position = num % wlength;
			}
			wirte = writeExpression[position];
			return getDataSource(wirte);
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
			if (originalReadExpression != null && (originalReadExpression.equals(md.originalReadExpression)))
				return true;
			return false;
		}
	}
}
