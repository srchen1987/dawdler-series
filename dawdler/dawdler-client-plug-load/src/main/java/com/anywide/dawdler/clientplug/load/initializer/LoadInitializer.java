package com.anywide.dawdler.clientplug.load.initializer;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.anywide.dawdler.clientplug.load.LoadListener;
import com.anywide.dawdler.core.annotation.Order;

@Order(1)
public class LoadInitializer implements ServletContainerInitializer {
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		ctx.addListener(LoadListener.class);
	}
}
