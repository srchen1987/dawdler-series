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
 package com.anywide.dawdler.clientplug.load;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFireHolder;
import com.anywide.dawdler.clientplug.web.WebControllerClassLoaderFire;
import com.anywide.dawdler.clientplug.web.filter.ViewFilter;
import com.anywide.dawdler.clientplug.web.listener.WebContextListenerProvider;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;
/**
 * 
 * @Title:  LoadListener.java   
 * @Description:    加载模版类的监听器   
 * @author: jackson.song    
 * @date:   2007年05月08日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
@WebListener
public class LoadListener implements ServletContextListener {
	private static Logger logger = LoggerFactory.getLogger(LoadListener.class);
	public static boolean DEBUG=true;
	private Map<LoadCore,Thread> threads = new ConcurrentHashMap<LoadCore,Thread>();
	private static long sleep=600000;
	public void contextDestroyed(ServletContextEvent arg0) {
		for(Iterator<Entry<LoadCore, Thread>> it = threads.entrySet().iterator();it.hasNext();){
			Entry<LoadCore,Thread> entry = it.next();
			entry.getKey().stop();
			if(entry.getValue().isAlive()){
				if(LoadListener.DEBUG)System.out.println("stop \t"+entry.getValue().getName()+"\tload");
				entry.getValue().interrupt();
			}
			 String filepath = DawdlerTool.getcurrentPath()+File.separator+entry.getKey().getHost()+".xml";
			 File file = new File(filepath);
			 if(file.exists())file.delete();
		}
		WebContextListenerProvider.listenerRun(false,arg0.getServletContext());
	}
	public void contextInitialized(ServletContextEvent arg0) {
		RemoteClassLoaderFireHolder.getInstance().addRemoteClassLoaderFire(new WebControllerClassLoaderFire());
		String debug = arg0.getServletContext().getInitParameter("debug");
		if(debug!=null&&debug.equals("debug"))DEBUG=true;
		XmlObject xml = ClientConfig.getInstance().getXml();
		for(Object o:xml.getNode("/config/loads-on/item")){
			Element ele = (Element) o;
			String host = ele.getText();
			if(LoadListener.DEBUG)System.out.println("starting load.....\t"+host+"\tmodule!");
			if(ele.attribute("sleep")!=null){
				try {
					sleep = Long.parseLong(ele.attributeValue("sleep"));
				} catch (Exception e) {
				}
			}
			String channelGroupId = ele.attributeValue("channel-group-id");
			LoadCore loadCore = new LoadCore(host,sleep,channelGroupId);
			try {
				loadCore.toCheck();
			} catch (IOException e) {
				logger.error("",e);
			}
			String mode = ele.attributeValue("mode");
			loadCore.initBeans();
			boolean run = mode==null?false:(mode.trim().equals("run")?true:false);
			if(!run){
				Thread thread = new Thread(loadCore,host+"LoadThread");
				thread.start();
				threads.put(loadCore,thread);
			}
		}
		WebContextListenerProvider.listenerRun(true,arg0.getServletContext());
//		Map<String, RequestUrlData> mappings = AnnotationUrlHandler.getUrlRules();
//		for(RequestUrlData rd : mappings.values()) {
//			injectRemoteService(rd.getTarget().getClass(), rd.getTarget());
//		}
		arg0.getServletContext().addFilter("ViewController",ViewFilter.class).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST,DispatcherType.FORWARD,DispatcherType.ERROR,DispatcherType.INCLUDE),true,"/*");
		
//		arg0.getServletContext().addFilter("ViewController",ViewController.class).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST,DispatcherType.FORWARD,DispatcherType.ERROR),true,"/*");
//		arg0.getServletContext().addFilter("ViewController",ViewControllerForLinuxsir.class).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST,DispatcherType.FORWARD,DispatcherType.ERROR),true,"/*");
	}
}
