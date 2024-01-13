package com.anywide.dawdler.clientplug.web.session.initializer;

import java.util.EnumSet;
import java.util.Set;

import com.anywide.dawdler.clientplug.web.session.DawdlerSessionFilter;
import com.anywide.dawdler.core.annotation.Order;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

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
