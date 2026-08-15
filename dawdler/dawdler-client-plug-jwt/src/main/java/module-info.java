import club.dawdler.clientplug.web.jwt.initializer.DawdlerJwtInitializer;

import jakarta.servlet.ServletContainerInitializer;

module dawdler.client.plug.jwt {
	requires java.base;
	requires dawdler.util;
	requires dawdler.core;
	requires org.slf4j;
	requires transitive jakarta.servlet;

	exports club.dawdler.clientplug.web.jwt;
	exports club.dawdler.clientplug.web.jwt.algorithm;
	exports club.dawdler.clientplug.web.jwt.claim;
	exports club.dawdler.clientplug.web.jwt.context;
	exports club.dawdler.clientplug.web.jwt.exception;
	exports club.dawdler.clientplug.web.jwt.operator;

	uses ServletContainerInitializer;

	provides ServletContainerInitializer with DawdlerJwtInitializer;
}
