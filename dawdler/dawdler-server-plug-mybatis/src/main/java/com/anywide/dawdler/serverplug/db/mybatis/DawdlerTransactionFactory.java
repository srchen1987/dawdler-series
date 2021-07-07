package com.anywide.dawdler.serverplug.db.mybatis;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

public class DawdlerTransactionFactory implements TransactionFactory {
	DawdlerMybatisTransaction dawdlerTransaction = new DawdlerMybatisTransaction();

	@Override
	public Transaction newTransaction(Connection conn) {
		return dawdlerTransaction;
	}

	@Override
	public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
		return dawdlerTransaction;
	}

}
