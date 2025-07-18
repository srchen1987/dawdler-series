import club.dawdler.clientplug.db.resource.TransactionLifeCycle;
import club.dawdler.core.component.resource.ComponentLifeCycle;

module dawdler.client.plug.db {
	requires java.base;
	requires dawdler.util;
	requires transitive dawdler.core;
	requires transitive java.sql;
	requires java.naming;
	requires org.slf4j;
	requires dawdler.db.core;
	requires dawdler.client.plug.local.service;
	requires dawdler.client.plug.web;

	provides ComponentLifeCycle with TransactionLifeCycle;

}
