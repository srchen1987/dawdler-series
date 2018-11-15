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
package com.anywide.dawdler.server.deploys;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.core.annotation.ListenerConfig;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.server.bean.ServicesBean;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.filter.DawdlerFilter;
import com.anywide.dawdler.server.filter.FilterProvider;
import com.anywide.dawdler.server.listener.DawdlerListenerProvider;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;
import com.anywide.dawdler.server.serivce.ServiceFactory;
import com.anywide.dawdler.server.serivce.ServicesManager;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateListener;
import com.anywide.dawdler.server.thread.processor.DefaultServiceExecutor;
import com.anywide.dawdler.server.thread.processor.ServiceExecutor;
import com.anywide.dawdler.util.SunReflectionFactoryInstantiator;
/**
 * 
 * @Title:  ServiceBase.java
 * @Description:    deploy下服务模块具体实现类   
 * @author: jackson.song    
 * @date:   2015年03月22日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class ServiceBase implements Service{
	private static Logger logger = LoggerFactory.getLogger(ServiceBase.class);
	private static final String CLASSESPATH = "classes";
	private static final String LIBPATH = "lib";
	public static final String SERVICEEXECUTOR_PREFIX= "serviceExecutor_prefix";
	private DawdlerDeployClassLoader classLoader;
	private File deploy;
	private DawdlerContext dawdlerContext;
	private static volatile boolean complete = false;
	private Semaphore semapphore = new Semaphore(0);
	private ServiceExecutor defaultServiceExecutor = new DefaultServiceExecutor();
	private ServiceExecutor serviceExecutor = defaultServiceExecutor;
	private DawdlerListenerProvider dawdlerListenerProvider = new DawdlerListenerProvider();
	private ServicesManager servicesManager = new ServicesManager();
	private FilterProvider filterProvider = new FilterProvider();
	public ServiceBase(File deploy,ClassLoader parent) throws MalformedURLException {
		this.deploy=deploy;
		classLoader = DawdlerDeployClassLoader.createLoader(parent,getLibURL());
		dawdlerContext = new DawdlerContext(classLoader,deploy.getName(),deploy.getPath(),getClassesDir().getPath(),servicesManager);
		classLoader.setDawdlerContext(dawdlerContext);
		Thread.currentThread().setContextClassLoader(classLoader);
	}
	public ServicesBean getServiesBean(String name){
		if(!complete){
			try {
				semapphore.acquire();
			} catch (InterruptedException e) {
				logger.error("",e);
			}
		}
		return servicesManager.getService(name);
	} 
	public ServicesBean getServiesBeanNoSafe(String name){
		return servicesManager.getService(name);
	} 
	private File getClassesDir(){
		return new File(deploy,CLASSESPATH);
	}
	private URL[] getLibURL() throws MalformedURLException{
		File file = new File(deploy,LIBPATH);
		return PathUtils.getLibURL(file,getClassesDir().toURI().toURL());
	}
	public Class<?> getClass(String className) throws ClassNotFoundException{
		return classLoader.loadClass(className);
	}
	@Override
	public void start() throws Exception {
//		Set<Class<?>> classes = DeployClassesScanner.getAppClasses("");
		try {
			Class<?> clazz = classLoader.loadClass("com.anywide.dawdler.serverplug.init.PlugInit");
			clazz.getConstructor(DawdlerContext.class).newInstance(dawdlerContext);
		} catch (Exception e) {
		}
		Object obj = dawdlerContext.getAttribute(SERVICEEXECUTOR_PREFIX);
		if(obj!=null)serviceExecutor=(ServiceExecutor) obj;
		Set<Class<?>> classes = DeployClassesScanner.getClassesInPath(deploy);
		Set<Class<?>> tmpClasses = new HashSet<>();
		for(Class<?> c : classes){
			if(((c.getModifiers()&1024)!=1024)&&((c.getModifiers()&16)!=16)&&((c.getModifiers()&16384)!=16384)&&((c.getModifiers()&8192)!=8192)&&((c.getModifiers()&512)!=512)){
				if(DawdlerServiceListener.class.isAssignableFrom(c)){
						Order order = c.getAnnotation(Order.class);
						DawdlerServiceListener dl = (DawdlerServiceListener) SunReflectionFactoryInstantiator.newInstance(c);
						OrderData<DawdlerServiceListener> orderData = new OrderData<>();
						orderData.setData(dl);
						if(order!=null)orderData.setOrder(order.value());
						dawdlerListenerProvider.addHandlerInterceptors(dl);
				}
				if(DawdlerServiceCreateListener.class.isAssignableFrom(c)){
					Order order = c.getAnnotation(Order.class);
					DawdlerServiceCreateListener dl = (DawdlerServiceCreateListener) SunReflectionFactoryInstantiator.newInstance(c);
					OrderData<DawdlerServiceCreateListener> orderData = new OrderData<>();
					orderData.setData(dl);
					if(order!=null)orderData.setOrder(order.value());
					servicesManager.getDawdlerServiceCreateProvider().addHandlerInterceptors(dl);
				}
				if(DawdlerFilter.class.isAssignableFrom(c)){
					Order order = c.getAnnotation(Order.class);
					DawdlerFilter filter = (DawdlerFilter) SunReflectionFactoryInstantiator.newInstance(c);
					OrderData<DawdlerFilter> orderData = new OrderData<>();
					orderData.setData(filter);
					if(order!=null)orderData.setOrder(order.value());
					filterProvider.addFilter(filter);
				}
				
				if(servicesManager.isService(c)) {
					tmpClasses.add(c);
				}
			}
		}
		for(Class<?> c:tmpClasses) {
			servicesManager.smartRegister(c);
		}
		servicesManager.fireCreate(dawdlerContext);
		dawdlerListenerProvider.order();
		servicesManager.getDawdlerServiceCreateProvider().order();
		filterProvider.orderAndbuildChain();
		
		for(OrderData<DawdlerServiceListener> data : dawdlerListenerProvider.getListeners()) {
			injectService(data.getData());
		}
		for(OrderData<DawdlerServiceListener> orderData : dawdlerListenerProvider.getListeners()){
			ListenerConfig listenerConfig = orderData.getClass().getAnnotation(ListenerConfig.class);
			if(listenerConfig!=null&&listenerConfig.asyn()) {
				 new Thread(()->{
					 if(listenerConfig.delayMsec()>0) {
						 try {
							Thread.sleep(listenerConfig.delayMsec());
						} catch (InterruptedException e) {
						}
							orderData.getData().contextInitialized(dawdlerContext);
					 }
				 }) .start();
			}else {
				orderData.getData().contextInitialized(dawdlerContext);
			}
		}
		complete=true;
		semapphore.release(Integer.MAX_VALUE);
	}
	public FilterProvider getFilterProvider() {
		return filterProvider;
	}
	@Override
	public void stop() {
		if(dawdlerListenerProvider.getListeners()!=null){
			for(int i = dawdlerListenerProvider.getListeners().size();i>0;i--){
				try {
					dawdlerListenerProvider.getListeners().get(i-1).getData().contextDestroyed(dawdlerContext);
				}catch(Exception e){
					logger.error("",e);
				}
			}
		}
		semapphore.release(Integer.MAX_VALUE);
		servicesManager.clear();
	}
	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}
	@Override
	public ServiceExecutor getServiceExecutor() {
		return serviceExecutor;
	}
	private void injectService(Object service) {
		Field[] fields = service.getClass().getDeclaredFields();
		for (Field filed : fields) {
			RemoteService remoteService = filed.getAnnotation(RemoteService.class);
			if (!filed.getType().isPrimitive()) {
				Class<?> serviceClass = filed.getType();
				filed.setAccessible(true);
				try {
					Object obj = null;
					if (remoteService != null) {
						if(!remoteService.remote()) {
							obj = ServiceFactory.getService(serviceClass,serviceExecutor,dawdlerContext);
						}else {
							Class c = classLoader.loadClass("com.anywide.dawdler.client.ServiceFactory");
							Method method = c.getMethod("getService",Class.class,String.class);
							String groupName = (remoteService.value()==null||remoteService.value().equals(""))?"defaultgroup":remoteService.value();
							obj = method.invoke(null, serviceClass,groupName);
						}
						if(obj != null)
							filed.set(service,obj);
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}
}

