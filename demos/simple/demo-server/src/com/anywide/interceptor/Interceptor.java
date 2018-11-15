package com.anywide.interceptor;

import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;

public class Interceptor implements HandlerInterceptor{

	@Override
	public void afterCompletion(TransactionController arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		System.out.println("HandlerInterceptor3");

	}

	@Override
	public void postHandle(TransactionController arg0, Throwable arg1) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("HandlerInterceptor2");

	}

	@Override
	public boolean preHandle(TransactionController arg0) throws Exception {
		System.out.println("HandlerInterceptor1");

		String path=arg0.getRequest().getContextPath();
		String basePath=arg0.getRequest().getScheme()+"://"+arg0.getRequest().getServerName()+":"+arg0.getRequest().getServerPort()+path+"/";
		arg0.setRequestAttribute("basePath", basePath);
		System.out.println("basePath"+basePath);
		return true;
	}

}
