import com.anywide.dawdler.clientplug.load.initializer.LoadInitializer;

import jakarta.servlet.ServletContainerInitializer;

module dawdler.client.plug.load {
	requires java.base;
	requires dawdler.util;
	requires dawdler.core;
	requires dawdler.serialization;
	requires dawdler.client;
	requires dawdler.load.bean;
	requires org.slf4j;
	requires dawdler.client.plug.validator;
	requires jakarta.servlet;
	requires dawdler.jakarta.fileupload;
	requires com.fasterxml.jackson.annotation;
	requires dawdler.client.plug.web;

	exports com.anywide.dawdler.clientplug.load;

	uses ServletContainerInitializer;

	provides ServletContainerInitializer with LoadInitializer;

}