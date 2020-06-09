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
package com.anywide.dawdler.serverplug.util;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.ToolEL;
import com.anywide.dawdler.util.XmlObject;
/**
 * 
 * @Title:  XmlConfig.java   
 * @Description:    简单的xml操作类   
 * @author: jackson.song    
 * @date:   2007年07月22日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class XmlConfig {
	private static Logger logger = LoggerFactory.getLogger(XmlConfig.class);
	private static long updatetime = 0;
	private static final String CONFIGPATH = "src_config.xml";
	private static XmlObject xmlobject = null;
	private static Map<String, Map<String, String>> datas = Collections.synchronizedMap(new HashMap<>());
	static {
		isUpdate();
		loadXML();
	}

	private XmlConfig() {
	}


	public static XmlObject getConfig() {
		if (isUpdate()) {
			loadXML();
		}
		return xmlobject;
	}

	public static String getRemoteLoad() {
		if (isUpdate()) {
			loadXML();
		}
		Element ele = (Element) xmlobject.getRoot().selectSingleNode("/config/remote_load");
		if (ele == null)
			System.err.println(CONFIGPATH + "config\remote_load not found！");
		String path = ele.attributeValue("package").replace("${classpath}", DawdlerTool.getcurrentPath());
		return path;
	}

	private static boolean isUpdate() {
		File file = new File(DawdlerTool.getcurrentPath() + File.separator + CONFIGPATH);
		if (!file.exists()) {
			System.out.println("not found " + CONFIGPATH);
		}
		if (updatetime != file.lastModified()) {
			updatetime = file.lastModified();
			return true;
		}
		return false;
	}

	public static Map<String, Map<String, String>> getDatas() {
		if (isUpdate()) {
			loadXML();
		}
		return datas;
	}


	private static void loadXML() {
		try {
			xmlobject = XmlObject.loadClassPathXML(File.separator + CONFIGPATH);
		} catch (DocumentException | IOException e) {
			logger.error("", e);
		}
		loadDataSource();
	}
	

	private static void loadDataSource() {
		List<Element> list = xmlobject.getRoot().selectNodes("/config/server-datas/server-data");
		for (Iterator<Element> it = list.iterator(); it.hasNext();) {
			Element ele = it.next();
			Map items = new HashMap();
			for (Iterator its = ele.attributes().iterator(); its.hasNext();) {
				Attribute ab = (Attribute) its.next();
				items.put(ab.getName(), ab.getValue());
			}
			datas.put(ele.attributeValue("id"), items);
		}
	}

}
