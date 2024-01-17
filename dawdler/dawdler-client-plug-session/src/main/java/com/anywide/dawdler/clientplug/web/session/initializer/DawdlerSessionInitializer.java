package com.anywide.dawdler.clientplug.web.session.initializer;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.anywide.dawdler.clientplug.web.session.DawdlerSessionFilter;
import com.anywide.dawdler.core.annotation.Order;

@Order(0)
public class DawdlerSessionInitializer implements ServletContainerInitializer {

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		EnumSet<DispatcherType> dispatcherType = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD,
				DispatcherType.ERROR, DispatcherType.INCLUDE);
		ctx.addFilter(DawdlerSessionFilter.class.getName(), DawdlerSessionFilter.class)
				.addMappingForUrlPatterns(dispatcherType, true, "/*");
	}
}
