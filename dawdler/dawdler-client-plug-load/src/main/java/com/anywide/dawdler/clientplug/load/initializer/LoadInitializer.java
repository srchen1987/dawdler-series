package com.anywide.dawdler.clientplug.load.initializer;

import java.util.Set;

import com.anywide.dawdler.clientplug.load.LoadListener;
import com.anywide.dawdler.core.annotation.Order;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

@Order(1)
public class LoadInitializer implements ServletContainerInitializer {
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		ctx.addListener(LoadListener.class);
	}
}
