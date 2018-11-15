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
package com.anywide.dawdler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @Title:  LogUtil.java
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2007年04月16日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class LogUtil {
	public static String getLineInfo() {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		return ste.getFileName() + ": Line " + ste.getLineNumber()+"\t"+Thread.currentThread().getName()+ "\t";
	}
	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(LogUtil.class);
		Exception exception = new NullPointerException("null");
		logger.trace("======trace");  
		logger.debug("======debug");  
		logger.info("======info");  
		logger.warn("======warn");  
		logger.error("======error",exception);  
	}

}
