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
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.util.JsonProcessUtil;
/**
 * 
 * @Title:  JsonDisplayPlug.java   
 * @Description:    json的实现   
 * @author: jackson.song    
 * @date:   2007年04月21日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class JsonDisplayPlug extends AbstractDisplayPlug{
	private static Logger logger = LoggerFactory.getLogger(JsonDisplayPlug.class);
	public JsonDisplayPlug(ServletContext servletContext) {
		super(servletContext);
	}
	@Override
	public void display(ViewForward wf) {
		logException(wf);
		HttpServletResponse response = wf.getResponse();
		response.setContentType(MIME_TYPE_JSON);
		String json = null;
		if (wf.getInvokeException() != null) {
			logger.error("",wf.getInvokeException());
			response.setStatus(500);
			wf.putData("status",500);
			wf.putData("message","Internal Server Error.");
			print(response,JsonProcessUtil.beanToJson(wf.getData()));
			return;
		}
		switch (wf.getStatus()) {
		case SUCCESS:
			json=JsonProcessUtil.beanToJson(wf.getData());
			break;
		case ERROR:
			wf.putData("message","Internal Server Error!");
			json=JsonProcessUtil.beanToJson(wf.getData());
			break;
		case REDIRECT: 
		case FORWARD:
		case STOP:
			if(wf.getData()!=null)
				json=JsonProcessUtil.beanToJson(wf.getData());
			break;
		}
		if(json!=null)
			print(response,json);
//		Map map = fw.getData();
	}
	private void print(HttpServletResponse response,String message){
		PrintWriter out = null;
		try {
			out =  response.getWriter();
			out.write(message);
			out.flush();
		} catch (Exception e) {
			logger.error("",e);
		}finally {
			if(out != null)out.close();
		}
	}

}

