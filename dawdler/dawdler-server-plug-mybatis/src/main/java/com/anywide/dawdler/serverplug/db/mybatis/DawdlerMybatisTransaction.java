package com.anywide.dawdler.serverplug.db.mybatis;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.transaction.Transaction;

import com.anywide.dawdler.serverplug.db.transaction.LocalConnectionFactory;
import com.anywide.dawdler.serverplug.db.transaction.SynReadConnectionObject;
import com.anywide.dawdler.util.TLS;

public class DawdlerMybatisTransaction implements Transaction {
	public static final String CURRENT_CONNECTION = "dmt_CURRENT_CONNECTION";

	@Override
	public Connection getConnection() throws SQLException {
		return (Connection) TLS.get(CURRENT_CONNECTION);
	}

	@Override
	public void commit() throws SQLException {

	}

	@Override
	public void rollback() throws SQLException {

	}

	@Override
	public void close() throws SQLException {

	}

	@Override
	public Integer getTimeout() throws SQLException {
		SynReadConnectionObject synReadObj = LocalConnectionFactory.getSynReadConnectionObject();
		if (synReadObj != null)
			return synReadObj.getDBTransaction().timeOut();
		return null;
	}

}
