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
package com.anywide.dawdler.clientplug.web.plugs;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;

/**
 * 
 * @Title:  AbstractDisplayPlug.java   
 * @Description:    抽象显示插件   
 * @author: jackson.song    
 * @date:   2007年04月21日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public abstract class AbstractDisplayPlug implements DisplayPlug {
	private static Logger logger = LoggerFactory.getLogger(AbstractDisplayPlug.class);
	protected final String MIME_TYPE_TEXT="text/html;charset=UTF-8";
	protected final String MIME_TYPE_JSON="application/json;charset=UTF-8";
	protected ServletContext servletContext;
	public AbstractDisplayPlug(ServletContext servletContext) {
		this.servletContext=servletContext;
	}
	protected void logException(ViewForward wf){
		if (wf.getInvokeException() != null) {
			logger.error("",wf.getInvokeException());
		}
	}

}

