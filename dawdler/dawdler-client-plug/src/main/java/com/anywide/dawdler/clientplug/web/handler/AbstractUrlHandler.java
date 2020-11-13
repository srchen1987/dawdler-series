/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.clientplug.web.handler;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.ViewControllerContext;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.interceptor.InterceptorProvider;
import com.anywide.dawdler.clientplug.web.plugs.DisplaySwitcher;
import com.anywide.dawdler.core.order.OrderData;

/**
 * 
 * @Title:  AbstractUrlHandler.java   
 * @Description:    urlHendler父类   
 * @author: jackson.song    
 * @date:   2007年04月18日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public abstract class AbstractUrlHandler{
	private List<OrderData<HandlerInterceptor>> handlerInterceptors=InterceptorProvider.getHandlerInterceptors();
//	protected TransactionControllerProxy transactionControllerProxy = new TransactionControllerProxy();
	public boolean preHandle(TransactionController tc) throws Exception {
		if(handlerInterceptors!=null)
		for(OrderData<HandlerInterceptor> handlerInterceptor:handlerInterceptors){
			if(!handlerInterceptor.getData().preHandle(tc))return false;
		}
		return true;
	}
	public void postHandle(TransactionController tc, Throwable ex) throws Exception {
		if(handlerInterceptors!=null){
			for(int i = handlerInterceptors.size();i>0;i--){
				handlerInterceptors.get(i-1).getData().postHandle(tc,ex);
			}
		}
	}
	public void afterCompletion(TransactionController tc,Throwable ex){
		if(handlerInterceptors!=null){
			for(int i = handlerInterceptors.size();i>0;i--){
				handlerInterceptors.get(i-1).getData().afterCompletion(tc,ex);
			}
		}
	}
	public abstract boolean handleUrl(String urishort,String method,HttpServletRequest request,HttpServletResponse response)throws ServletException;

	protected boolean invokeMethod(TransactionController targetobj,
			Method method, ViewForward wf) {
		try {
			if (!preHandle(targetobj))
				return true;
			method.invoke(targetobj, null);
		} catch (Exception e) {
			wf.setInvokeException(e);
		}
		try {
			postHandle(targetobj,wf.getInvokeException());
		} catch (Exception e) {
			wf.setInvokeException(e);
		}
		DisplaySwitcher.switchDisplay(wf);
		afterCompletion(targetobj, wf.getInvokeException());
		return true;
	}
	protected ViewForward createViewForward() {
		ViewForward wf = ViewControllerContext.getViewForward();
		return wf;
		
	}
	
}

