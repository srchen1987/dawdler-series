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
package com.anywide.dawdler.clientplug.web.validator.webbind;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.ViewControllerContext;
import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.plugs.PlugFactory;
import com.anywide.dawdler.clientplug.web.util.JsonProcessUtil;
import com.anywide.dawdler.clientplug.web.validator.ValidateParser;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlField;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator;
/**
 * 
 * @Title:  WebValidateExecutor.java   
 * @Description:    对web请求进行校验   
 * @author: jackson.song    
 * @date:   2007年07月23日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class WebValidateExecutor {
	public static final String VALIDATEERROR="validate_error";//验证错误
	private static Logger logger = LoggerFactory.getLogger(WebValidateExecutor.class);
	private static Map<Class, ControlValidator> validators = Collections.synchronizedMap(new HashMap<Class, ControlValidator>());
	public static boolean validate(HttpServletRequest request,HttpServletResponse response,Object arg2){
		if(!(arg2 instanceof TransactionController)){
			return true;
		}
		ViewForward viewForward =  ViewControllerContext.getViewForward();
		RequestMapping ra = viewForward.getRequestMapping();
		Map<String,Serializable> errors = new HashMap<>();
		ControlValidator cv = null;
		Class clazz = arg2.getClass();
		if(!validators.containsKey(clazz)){
			cv = ValidateResourceLoader.getControlValidator(clazz);
			validators.put(clazz,cv);
		}else{
			cv = validators.get(clazz);
		}
		String uri = viewForward.getUriShort();
		if(cv==null||uri==null)return true;
		Map<String,ControlField> rules =  cv.getMappings().get(uri);
		if(rules==null)rules=cv.getGlobalControlFields();
		if(rules!=null){
			if(ra!=null&&ra.generateValidator()&&!rules.isEmpty()){
				StringBuffer sb = new StringBuffer("sir_validate.addRule(");
				Collection<ControlField> cc = rules.values();
				List list = new ArrayList();
				for(Iterator<ControlField> it = cc.iterator();it.hasNext();){
					Map map = new LinkedHashMap();
					ControlField cf = it.next();
					map.put("id",cf.getFieldName());
					map.put("viewname",cf.getFieldExplain());
					map.put("validaterule",cf.getRules());
					list.add(map);
				}
				sb.append(JsonProcessUtil.beanToJson(list));
				sb.append(");\n");
				sb.append("sir_validate.buildFormValidate($formid);");
				System.out.println("######################################");
				System.out.println(sb.toString());
				System.out.println("######################################");
			}
			Set<Entry<String,ControlField>> set = rules.entrySet();
			for(Entry<String,ControlField> entry:set){
				String key = entry.getKey();
				ControlField cf = entry.getValue();
				Map params = viewForward.paramMaps();
				if(params==null)params = new HashMap();
				String error = ValidateParser.validate(cf.getFieldExplain(),params.get(cf.getFieldName()),cf.getRules());
				if(error!=null)errors.put(key,error);
			}
		}
		if(!errors.isEmpty()){
			if(ra!=null&&ra.input()!=null&&!ra.input().trim().equals("")){
//				ViewControllerContext.removeViewForward();
				request.setAttribute(VALIDATEERROR, errors);
				try {
					request.getRequestDispatcher(ra.input()).forward(request, response);
				} catch (ServletException | IOException e) {
					logger.error("",e);
				}
			}else{
				viewForward.putData(VALIDATEERROR, errors);
				PlugFactory.getDisplayPlug("json").display(viewForward);
			}
			return false;
		}
		return true;
	} 

 
}

