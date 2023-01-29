module dawdler.mybatis.core {
	requires java.base;
	requires transitive java.sql;
	requires org.slf4j;
	requires org.aspectj.runtime;
	requires cglib;
	requires javassist;
	requires transitive java.naming;
	requires ognl;
	requires dom4j;

	exports org.apache.ibatis.parsing;
	exports org.apache.ibatis.builder;
	exports org.apache.ibatis.binding;
	exports org.apache.ibatis.cursor;
	exports org.apache.ibatis.exceptions;
	exports org.apache.ibatis.executor;
	exports org.apache.ibatis.mapping;
	exports org.apache.ibatis.reflection;
	exports org.apache.ibatis.session;
	exports org.apache.ibatis.executor.result;
	exports org.apache.ibatis.transaction;
	exports org.apache.ibatis.builder.xml;
	exports org.apache.ibatis.io;
	exports org.apache.ibatis.type;
	exports org.apache.ibatis.plugin;
	exports org.apache.ibatis.annotations;
	exports org.apache.ibatis.reflection.factory;
	exports org.apache.ibatis.reflection.wrapper;
	exports org.apache.ibatis.transaction.managed;
}