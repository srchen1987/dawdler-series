module dawdler.dao.core {
	requires java.base;
	requires dawdler.util;
	requires dawdler.load.bean;
	requires org.slf4j;
	requires transitive java.sql;
	requires dawdler.db.core;
	
	exports com.anywide.dawdler.core.db.dao;
}