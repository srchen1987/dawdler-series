import com.anywide.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;
import com.anywide.dawdler.clientplug.web.bind.discoverer.impl.CompileParameterDiscoverer;
import com.anywide.dawdler.clientplug.web.bind.discoverer.impl.LocalVariableTableParameterDiscoverer;
import com.anywide.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.bind.resolver.impl.AnnotationMethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.bind.resolver.impl.BasicsTypeMethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.bind.resolver.impl.ServletMethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.bind.resolver.impl.ServletUploadMethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.classloader.DawdlerClassLoaderMatcher;
import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.web.component.injector.WebComponentInjector;
import com.anywide.dawdler.clientplug.web.fire.WebComponentClassLoaderFire;
import com.anywide.dawdler.clientplug.web.initializer.WebInitializer;
import com.anywide.dawdler.clientplug.web.plugs.DisplayPlug;
import com.anywide.dawdler.clientplug.web.plugs.impl.JsonDisplayPlug;
import com.anywide.dawdler.clientplug.web.plugs.impl.JspDisplayPlug;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;

import jakarta.servlet.ServletContainerInitializer;

module dawdler.client.plug.web {
	requires java.base;
	requires transitive dawdler.util;
	requires dawdler.core;
	requires dawdler.serialization;
	requires dawdler.load.bean;
	requires org.slf4j;
	requires dawdler.client.plug.validator;
	requires jakarta.servlet;
	requires dawdler.jakarta.fileupload;
	requires com.fasterxml.jackson.annotation;

	exports com.anywide.dawdler.clientplug.web.annotation;
	exports com.anywide.dawdler.clientplug.web.handler;
	exports com.anywide.dawdler.clientplug.web.plugs;
	exports com.anywide.dawdler.clientplug.web;
	exports com.anywide.dawdler.clientplug.web.interceptor;
	exports com.anywide.dawdler.clientplug.web.listener;
	exports com.anywide.dawdler.clientplug.web.upload;
	exports com.anywide.dawdler.clientplug.web.filter;
	exports com.anywide.dawdler.clientplug.web.classloader;
	exports com.anywide.dawdler.clientplug.web.conf;

	uses com.anywide.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;

	provides MethodArgumentResolver with AnnotationMethodArgumentResolver, BasicsTypeMethodArgumentResolver,
			ServletMethodArgumentResolver, ServletUploadMethodArgumentResolver;

	uses com.anywide.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;

	provides ParameterDiscoverer with CompileParameterDiscoverer, LocalVariableTableParameterDiscoverer;

	uses com.anywide.dawdler.clientplug.web.plugs.DisplayPlug;

	provides DisplayPlug with JsonDisplayPlug, JspDisplayPlug;

	uses RemoteClassLoaderFire;

	provides RemoteClassLoaderFire with WebComponentClassLoaderFire;

	uses CustomComponentInjector;

	provides CustomComponentInjector with WebComponentInjector;

	uses ServletContainerInitializer;

	provides ServletContainerInitializer with WebInitializer;

	uses DawdlerClassLoaderMatcher;

}