import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;
import com.anywide.dawdler.clientplug.web.bind.discoverer.impl.CompileParameterDiscoverer;
import com.anywide.dawdler.clientplug.web.bind.discoverer.impl.LocalVariableTableParameterDiscoverer;
import com.anywide.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.bind.resolver.impl.AnnotationMethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.bind.resolver.impl.BasicsTypeMethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.bind.resolver.impl.ServletMethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.bind.resolver.impl.ServletUploadMethodArgumentResolver;
import com.anywide.dawdler.clientplug.web.fire.WebComponentClassLoaderFire;
import com.anywide.dawdler.clientplug.web.plugs.DisplayPlug;
import com.anywide.dawdler.clientplug.web.plugs.impl.JsonDisplayPlug;
import com.anywide.dawdler.clientplug.web.plugs.impl.JspDisplayPlug;

module dawdler.client.plug {
	requires java.base;
	requires dawdler.util;
	requires dawdler.core;
	requires dawdler.serialization;
	requires dawdler.client;
	requires dawdler.load.bean;
	requires dom4j;
	requires org.slf4j;
	requires transitive dawdler.client.plug.validator;
	requires transitive jakarta.servlet;
	requires dawdler.jakarta.fileupload;
	requires com.fasterxml.jackson.annotation;

	exports com.anywide.dawdler.clientplug.annotation;
	exports com.anywide.dawdler.clientplug.web.handler;
	exports com.anywide.dawdler.clientplug.web.plugs;
	exports com.anywide.dawdler.clientplug.load.classloader;
	exports com.anywide.dawdler.clientplug.web;
	exports com.anywide.dawdler.clientplug.web.interceptor;
	exports com.anywide.dawdler.clientplug.web.listener;
	exports com.anywide.dawdler.clientplug.web.upload;

	uses RemoteClassLoaderFire;

	provides RemoteClassLoaderFire with WebComponentClassLoaderFire;

	uses com.anywide.dawdler.clientplug.web.bind.resolver.MethodArgumentResolver;

	provides MethodArgumentResolver with AnnotationMethodArgumentResolver, BasicsTypeMethodArgumentResolver,
			ServletMethodArgumentResolver, ServletUploadMethodArgumentResolver;

	uses com.anywide.dawdler.clientplug.web.bind.discoverer.ParameterDiscoverer;

	provides ParameterDiscoverer with CompileParameterDiscoverer, LocalVariableTableParameterDiscoverer;

	uses com.anywide.dawdler.clientplug.web.plugs.DisplayPlug;

	provides DisplayPlug with JsonDisplayPlug, JspDisplayPlug;
}