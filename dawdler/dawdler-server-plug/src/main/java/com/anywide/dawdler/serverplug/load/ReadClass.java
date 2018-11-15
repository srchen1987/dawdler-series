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
package com.anywide.dawdler.serverplug.load;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.serverplug.load.bean.RemoteFiles;
import com.anywide.dawdler.serverplug.load.bean.RemoteFiles.RemoteFile;
import com.anywide.dawdler.serverplug.util.XmlConfig;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

/**
 * 
 * @Title: ReadClass.java
 * @Description: 读取服务端类模版
 * @author: jackson.song
 * @date: 2007年09月07日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ReadClass {
	private static Logger logger = LoggerFactory.getLogger(ReadClass.class);
	private static Pattern p = Pattern.compile("(.*)\\.class$");

	public static XmlObject read(String host) {
		String path = DawdlerTool.getcurrentPath();
		try {
			XmlObject xml = new XmlObject(XmlConfig.getRemoteLoad());
			List<Element> hostlist = (List<Element>) xml.getNode("/hosts/host[@name='" + host + "']/package");
			if (hostlist == null || hostlist.isEmpty())
				return null;
			XmlObject xmlo = new XmlObject();
			xmlo.CreateRoot("hosts");
			Element root = xmlo.getRoot();
			for (Element hostele : hostlist) {
				String type = hostele.attributeValue("type");
				boolean isbean = type != null && type.trim().equals("bean");
				String pack = hostele.getTextTrim().replace(".", File.separator);
				File file = new File(path + pack);
				if (!file.isDirectory())
					throw new FileNotFoundException(
							"not exist\t" + path + pack + "\t or " + path + pack + " is not Directory!");
				createXmlObjectByFile(root, file, pack, host, isbean);
			}
			return xmlo;
		} catch (DocumentException e) {
			logger.error("", e);
			return null;
		} catch (IOException e) {
			logger.error("", e);
			return null;
		}

	}

	private static void createXmlObjectByFile(Element hosts, File file, String pack, String host, boolean isbean) {
		Element hostele = hosts.addElement("host");
		hostele.addAttribute("type", isbean ? "bean" : "controller");
		for (File fs : file.listFiles()) {
			String s = fs.getName();
			Matcher match = p.matcher(s);
			if (match.find()) {
				File f = new File(file.getPath() + File.separator + s);
				Element item = hostele.addElement("item");
				// item.addAttribute("name",fs.getAbsolutePath());
				item.addAttribute("name", match.group(1).toLowerCase());
				item.addAttribute("checkname", fs.getAbsolutePath().replace(DawdlerTool.getcurrentPath(), ""));
				item.addAttribute("package", pack);
				item.addAttribute("update", "" + f.lastModified());
				item.addText(pack.replace(File.separator, ".") + "." + s);
			}
		}
	}

	public static RemoteFiles operation(String[] filenames) throws FileNotFoundException {
		RemoteFiles rfs = new RemoteFiles();
		List files = new ArrayList();
		String path = DawdlerTool.getcurrentPath();
		for (String name : filenames) {
			Matcher match = p.matcher(name);
			if (match.find()) {
				String temname = match.group(1).replace(".", File.separator);
				File file = new File(path + File.separator + temname + ".class");
				if (file.exists()) {
					RemoteFile rf = new RemoteFiles().new RemoteFile();
					rf.setFilename(name);
					InputStream in = new FileInputStream(file);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] tempbytes = new byte[1024];
					int tempsize;
					try {
						while ((tempsize = in.read(tempbytes)) != -1) {
							baos.write(tempbytes, 0, tempsize);
						}
						baos.flush();
						byte[] tem = baos.toByteArray();
						rf.setData(tem);
						files.add(rf);
					} catch (Exception e) {
						logger.error("", e);
					}
					finally {
						try {
							in.close();
							baos.close();
						} catch (Exception e) {
							logger.error("", e);
						}
					}
					
				}
			}
		}
		rfs.setFiles(files);
		return rfs;
	}
}
