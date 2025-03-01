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
package com.anywide.dawdler.core.db.mybatis.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementUtil;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * @author jackson.song
 * @version V1.0
 * 无缓存执行器 因为优化将session改为single模式 所以不支持其他的BatchExecutor, ReuseExecutor有batch的需求可以采用jdbc batch方式处理 效率更高
 */
public abstract class NoCacheBaseExecutor implements Executor {

	protected Configuration configuration;

	protected Transaction transaction;

	protected NoCacheBaseExecutor(Configuration configuration, Transaction transaction) {
		this.configuration = configuration;
		this.transaction = transaction;
	}

	@Override
	public Transaction getTransaction() {
		return null;
	}

	@Override
	public void close(boolean forceRollback) {

	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public int update(MappedStatement ms, Object parameter) throws SQLException {
		ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
		return doUpdate(ms, parameter);
	}

	@Override
	public List<BatchResult> flushStatements() throws SQLException {
		return flushStatements(false);
	}

	public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
		return doFlushStatements(isRollBack);
	}

	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
			throws SQLException {
		BoundSql boundSql = ms.getBoundSql(parameter);
		return query(ms, parameter, rowBounds, resultHandler, null, boundSql);
	}

	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
			CacheKey key, BoundSql boundSql) throws SQLException {
		return queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
	}

	@Override
	public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
		BoundSql boundSql = ms.getBoundSql(parameter);
		return doQueryCursor(ms, parameter, rowBounds, boundSql);
	}

	@Override
	public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key,
			Class<?> targetType) {
	}

	@Override
	public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
		return null;
	}

	@Override
	public boolean isCached(MappedStatement ms, CacheKey key) {
		return false;
	}

	@Override
	public void commit(boolean required) throws SQLException {
	}

	@Override
	public void rollback(boolean required) throws SQLException {
	}

	@Override
	public void clearLocalCache() {
	}

	protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;

	protected abstract List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException;

	protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds,
			ResultHandler resultHandler, BoundSql boundSql) throws SQLException;

	protected abstract <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds,
			BoundSql boundSql) throws SQLException;

	protected void closeStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
	}

	protected void applyTransactionTimeout(Statement statement) throws SQLException {
		StatementUtil.applyTransactionTimeout(statement, statement.getQueryTimeout(), transaction.getTimeout());
	}

	private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds,
			ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
		return doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
	}

	@Override
	public void setExecutorWrapper(Executor wrapper) {
	}

	protected Connection getConnection(Log statementLog) throws SQLException {
		Connection connection = transaction.getConnection();
		if (statementLog.isDebugEnabled()) {
			return ConnectionLogger.newInstance(connection, statementLog, 1);
		}
		return connection;
	}

}
