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
package com.anywide.dawdler.core.db.mybatis;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.db.mybatis.session.DawdlerSqlSessionFactory;
import com.anywide.dawdler.util.spring.antpath.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * dawdler实现session工厂构建器
 */
public class DawdlerSqlSessionFactoryBuilder extends SqlSessionFactoryBuilder {
	private final static Logger logger = LoggerFactory.getLogger(DawdlerSqlSessionFactoryBuilder.class);

	private String configLocation;

	private Configuration configuration;

	private List<Resource> mapperLocations;

	private TransactionFactory transactionFactory;

	private Properties configurationProperties;

	private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();

	private SqlSessionFactory sqlSessionFactory;

	private String environment = DawdlerSqlSessionFactoryBuilder.class.getSimpleName();

	private Interceptor[] plugins;

	private TypeHandler<?>[] typeHandlers;

	private String typeHandlersPackage;

	private Class<?>[] typeAliases;

	private String typeAliasesPackage;

	private Class<?> typeAliasesSuperType;

	private Class<? extends VFS> vfs;

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public TransactionFactory getTransactionFactory() {
		return transactionFactory;
	}

	public void setTransactionFactory(TransactionFactory transactionFactory) {
		this.transactionFactory = transactionFactory;
	}

	public Properties getConfigurationProperties() {
		return configurationProperties;
	}

	public void setConfigurationProperties(Properties configurationProperties) {
		this.configurationProperties = configurationProperties;
	}

	public SqlSessionFactoryBuilder getSqlSessionFactoryBuilder() {
		return sqlSessionFactoryBuilder;
	}

	public void setSqlSessionFactoryBuilder(SqlSessionFactoryBuilder sqlSessionFactoryBuilder) {
		this.sqlSessionFactoryBuilder = sqlSessionFactoryBuilder;
	}

	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public Interceptor[] getPlugins() {
		return plugins;
	}

	public void setPlugins(Interceptor[] plugins) {
		this.plugins = plugins;
	}

	public TypeHandler<?>[] getTypeHandlers() {
		return typeHandlers;
	}

	public void setTypeHandlers(TypeHandler<?>[] typeHandlers) {
		this.typeHandlers = typeHandlers;
	}

	public String getTypeHandlersPackage() {
		return typeHandlersPackage;
	}

	public void setTypeHandlersPackage(String typeHandlersPackage) {
		this.typeHandlersPackage = typeHandlersPackage;
	}

	public Class<?>[] getTypeAliases() {
		return typeAliases;
	}

	public void setTypeAliases(Class<?>[] typeAliases) {
		this.typeAliases = typeAliases;
	}

	public String getTypeAliasesPackage() {
		return typeAliasesPackage;
	}

	public void setTypeAliasesPackage(String typeAliasesPackage) {
		this.typeAliasesPackage = typeAliasesPackage;
	}

	public Class<?> getTypeAliasesSuperType() {
		return typeAliasesSuperType;
	}

	public void setTypeAliasesSuperType(Class<?> typeAliasesSuperType) {
		this.typeAliasesSuperType = typeAliasesSuperType;
	}

	public Class<? extends VFS> getVfs() {
		return vfs;
	}

	public void setVfs(Class<? extends VFS> vfs) {
		this.vfs = vfs;
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public ObjectWrapperFactory getObjectWrapperFactory() {
		return objectWrapperFactory;
	}

	public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
		this.objectWrapperFactory = objectWrapperFactory;
	}

	private ObjectFactory objectFactory;

	private ObjectWrapperFactory objectWrapperFactory;

	public SqlSessionFactory buildSqlSessionFactory() throws Exception {

		Configuration configuration;
		XMLConfigBuilder xmlConfigBuilder = null;
		if (this.configuration != null) {
			configuration = this.configuration;
			if (configuration.getVariables() == null) {
				configuration.setVariables(this.configurationProperties);
			} else if (this.configurationProperties != null) {
				configuration.getVariables().putAll(this.configurationProperties);
			}
		} else if (this.configLocation != null) {
			InputStream input = null;
			try {
				input = new FileInputStream(configLocation);
				xmlConfigBuilder = new XMLConfigBuilder(input, null, this.configurationProperties);
				configuration = xmlConfigBuilder.getConfiguration();
			} finally {
				if (input != null) {
					input.close();
				}
			}

		} else {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Property 'configuration' or 'configLocation' not specified, using default MyBatis Configuration");
			}
			configuration = new Configuration();
			if (this.configurationProperties != null) {
				configuration.setVariables(this.configurationProperties);
			}
		}

		if (this.objectFactory != null) {
			configuration.setObjectFactory(this.objectFactory);
		}

		if (this.objectWrapperFactory != null) {
			configuration.setObjectWrapperFactory(this.objectWrapperFactory);
		}

		if (this.vfs != null) {
			configuration.setVfsImpl(this.vfs);
		}
		if (this.typeAliasesPackage != null && !typeAliasesPackage.trim().equals("")) {
			String[] typeAliasPackageArray = typeAliasesPackage.split(";");
			for (String packageToScan : typeAliasPackageArray) {
				configuration.getTypeAliasRegistry().registerAliases(packageToScan,
						typeAliasesSuperType == null ? Object.class : typeAliasesSuperType);
				if (logger.isDebugEnabled()) {
					logger.debug("Scanned package: '" + packageToScan + "' for aliases");
				}
			}
		}

		if (typeAliases != null) {
			for (Class<?> typeAlias : this.typeAliases) {
				configuration.getTypeAliasRegistry().registerAlias(typeAlias);
				if (logger.isDebugEnabled()) {
					logger.debug("Registered type alias: '" + typeAlias + "'");
				}
			}
		}

		if (plugins != null) {
			for (Interceptor plugin : this.plugins) {
				configuration.addInterceptor(plugin);
				if (logger.isDebugEnabled()) {
					logger.debug("Registered plugin: '" + plugin + "'");
				}
			}
		}

		if (typeHandlersPackage != null && !typeHandlersPackage.trim().equals("")) {
			String[] typeHandlersPackageArray = typeHandlersPackage.split(";");
			for (String packageToScan : typeHandlersPackageArray) {
				configuration.getTypeHandlerRegistry().register(packageToScan);
				if (logger.isDebugEnabled()) {
					logger.debug("Scanned package: '" + packageToScan + "' for type handlers");
				}
			}
		}

		if (typeHandlers != null) {
			for (TypeHandler<?> typeHandler : this.typeHandlers) {
				configuration.getTypeHandlerRegistry().register(typeHandler);
				if (logger.isDebugEnabled()) {
					logger.debug("Registered type handler: '" + typeHandler + "'");
				}
			}
		}
		if (xmlConfigBuilder != null) {
			try {
				xmlConfigBuilder.parse();

				if (logger.isDebugEnabled()) {
					logger.debug("Parsed configuration file: '" + this.configLocation + "'");
				}
			} catch (Exception ex) {
				throw new Exception("Failed to parse config resource: " + this.configLocation, ex);
			} finally {
				ErrorContext.instance().reset();
			}
		}
		configuration.setEnvironment(new Environment(this.environment, this.transactionFactory, new DataSource() {

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				return null;
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				return false;
			}

			@Override
			public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
				return null;
			}

			@Override
			public void setLoginTimeout(int seconds) throws SQLException {
			}

			@Override
			public void setLogWriter(PrintWriter out) throws SQLException {
			}

			@Override
			public int getLoginTimeout() throws SQLException {
				return 0;
			}

			@Override
			public PrintWriter getLogWriter() throws SQLException {
				return null;
			}

			@Override
			public Connection getConnection(String username, String password) throws SQLException {
				return null;
			}

			@Override
			public Connection getConnection() throws SQLException {
				return null;
			}
		}));

		if (this.mapperLocations != null) {
			for (Resource mapperLocation : this.mapperLocations) {
				InputStream input = null;
				try {
					input = mapperLocation.getInputStream();
					XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(input, configuration,
							mapperLocation.getURI().toString(), configuration.getSqlFragments());
					xmlMapperBuilder.parse();
				} catch (Exception e) {
					throw new IOException("Failed to parse mapping resource: ", e);
				} finally {
					ErrorContext.instance().reset();
					if (input != null) {
						input.close();
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Parsed mapper file: '" + mapperLocation + "'");
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Property 'mapperLocations' was not specified or no matching resources found");
			}
		}
		return sqlSessionFactory = build(configuration);
	}

	public String getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	public List<Resource> getMapperLocations() {
		return mapperLocations;
	}

	public void setMapperLocations(List<Resource> mapperLocations) {
		this.mapperLocations = mapperLocations;
	}

	public SqlSessionFactory build(Configuration config) {
		return new DawdlerSqlSessionFactory(config);
	}
}
