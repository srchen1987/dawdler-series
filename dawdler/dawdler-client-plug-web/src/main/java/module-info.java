import club.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;
import club.dawdler.clientplug.web.bind.discoverer.impl.CompileParameterDiscoverer;
import club.dawdler.clientplug.web.bind.discoverer.impl.LocalVariableTableParameterDiscoverer;
import club.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;
import club.dawdler.clientplug.web.bind.resolver.impl.AnnotationMethodArgumentResolver;
import club.dawdler.clientplug.web.bind.resolver.impl.BasicsTypeMethodArgumentResolver;
import club.dawdler.clientplug.web.bind.resolver.impl.ServletMethodArgumentResolver;
import club.dawdler.clientplug.web.bind.resolver.impl.ServletUploadMethodArgumentResolver;
import club.dawdler.clientplug.web.classloader.DawdlerClassLoaderMatcher;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import club.dawdler.clientplug.web.component.injector.WebComponentInjector;
import club.dawdler.clientplug.web.fire.WebComponentClassLoaderFire;
import club.dawdler.clientplug.web.initializer.WebInitializer;
import club.dawdler.clientplug.web.plugs.DisplayPlug;
import club.dawdler.clientplug.web.plugs.impl.JsonDisplayPlug;
import club.dawdler.clientplug.web.plugs.impl.JspDisplayPlug;
import club.dawdler.core.component.injector.CustomComponentInjector;

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

	exports club.dawdler.clientplug.web.annotation;
	exports club.dawdler.clientplug.web.handler;
	exports club.dawdler.clientplug.web.plugs;
	exports club.dawdler.clientplug.web;
	exports club.dawdler.clientplug.web.interceptor;
	exports club.dawdler.clientplug.web.listener;
	exports club.dawdler.clientplug.web.upload;
	exports club.dawdler.clientplug.web.filter;
	exports club.dawdler.clientplug.web.classloader;
	exports club.dawdler.clientplug.web.conf;
	exports club.dawdler.clientplug.web.health;

	uses club.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;

	provides MethodArgumentResolver with AnnotationMethodArgumentResolver, BasicsTypeMethodArgumentResolver,
			ServletMethodArgumentResolver, ServletUploadMethodArgumentResolver;

	uses club.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;

	provides ParameterDiscoverer with CompileParameterDiscoverer, LocalVariableTableParameterDiscoverer;

	uses club.dawdler.clientplug.web.plugs.DisplayPlug;

	provides DisplayPlug with JsonDisplayPlug, JspDisplayPlug;

	uses RemoteClassLoaderFire;

	provides RemoteClassLoaderFire with WebComponentClassLoaderFire;

	uses CustomComponentInjector;

	provides CustomComponentInjector with WebComponentInjector;

	uses ServletContainerInitializer;

	provides ServletContainerInitializer with WebInitializer;

	uses DawdlerClassLoaderMatcher;

}
