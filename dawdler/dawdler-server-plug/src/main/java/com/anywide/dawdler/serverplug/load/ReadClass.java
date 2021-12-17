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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.serverplug.load.bean.RemoteFiles;
import com.anywide.dawdler.serverplug.load.bean.RemoteFiles.RemoteFile;
import com.anywide.dawdler.serverplug.util.XmlConfig;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ReadClass.java
 * @Description 读取服务端类模版
 * @date 2007年9月07日
 * @email suxuan696@gmail.com
 */
public class ReadClass {
	private static final Logger logger = LoggerFactory.getLogger(ReadClass.class);
	private static final Pattern classPattern = Pattern.compile("(.*)\\.class$");
	private static String path = new File(DawdlerTool.getcurrentPath()).getPath()+File.separator;
	public static XmlObject read(String host) {
		try {
			XmlObject xml = new XmlObject(XmlConfig.getRemoteLoad());
			List<Node> hosts = xml.selectNodes("/hosts/host[@name='" + host + "']/package");
			if (hosts == null || hosts.isEmpty())
				return null;
			XmlObject xmlo = new XmlObject();
			xmlo.CreateRoot("hosts");
			Element root = xmlo.getRoot();
			for (Object hostObj : hosts) {
				Element hostEle = (Element) hostObj;
				String type = hostEle.attributeValue("type");
				boolean isbean = type != null && type.trim().equals("api");
				String pack = hostEle.getTextTrim().replace(".", File.separator);
				File file = new File(path + pack);
				if (!file.isDirectory())
					throw new FileNotFoundException(
							"not exist\t" + path + pack + "\t or " + path + pack + " is not directory!");
				createXmlObjectByFile(root, file, pack, host, isbean);
			}
			return xmlo;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}

	}

	private static void createXmlObjectByFile(Element hosts, File file, String pack, String host, boolean isbean) {
		Element hostele = hosts.addElement("host");
		hostele.addAttribute("type", isbean ? "api" : "component");
		for (File fs : file.listFiles()) {
			String s = fs.getName();
			Matcher match = classPattern.matcher(s);
			if (match.find()) {
				File f = new File(file.getPath() + File.separator + s);
				Element item = hostele.addElement("item");
				item.addAttribute("checkname", fs.getAbsolutePath().replace(path, ""));
				item.addAttribute("update", "" + f.lastModified());
			}
		}
	}

	public static RemoteFiles operation(String[] filenames) throws FileNotFoundException {
		RemoteFiles rfs = new RemoteFiles();
		List<RemoteFile> files = new ArrayList<>();
		String path = DawdlerTool.getcurrentPath();
		for (String name : filenames) {
			Matcher match = classPattern.matcher(name);
			if (match.find()) {
				String fileName = match.group(1).replace(".", File.separator);
				File file = new File(path + File.separator + fileName + ".class");
				if (file.exists()) {
					RemoteFile rf = new RemoteFiles().new RemoteFile();
					rf.setFilename(name);
					InputStream in = null;
					try {
						in = new FileInputStream(file);
					} catch (FileNotFoundException e) {
						logger.error("", e);
						throw e;
					}
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] data = new byte[2048];
					int position;
					try {
						while ((position = in.read(data)) != -1) {
							out.write(data, 0, position);
						}
						out.flush();
						rf.setData(out.toByteArray());
						files.add(rf);
					} catch (Exception e) {
						logger.error("", e);
					} finally {
						try {
							in.close();
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
