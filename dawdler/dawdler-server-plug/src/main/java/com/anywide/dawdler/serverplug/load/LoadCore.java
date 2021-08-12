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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.Transaction;
import com.anywide.dawdler.client.TransactionProvider;
import com.anywide.dawdler.serverplug.bean.XmlBean;
import com.anywide.dawdler.serverplug.load.bean.RemoteFiles;
import com.anywide.dawdler.serverplug.load.bean.RemoteFiles.RemoteFile;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title LoadCore.java
 * @Description 加载服务端类模版
 * @date 2007年9月05日
 * @email suxuan696@gmail.com
 */
public class LoadCore implements Runnable {
	private static final String PREFIX = ".dat";
	private static final Logger logger = LoggerFactory.getLogger(LoadCore.class);
	private static final Pattern CLASS_PATTERN = Pattern.compile("(.*)\\.class$");
	private static final String CLASSP_REFIX=".class";
	private static final String CURRENT_PATH;
	private static final String TYPE_API = "api";
	private static final String TYPE_COMPONENT = "component";
	static {
		CURRENT_PATH = DawdlerTool.getcurrentPath();
	}

	private final String host;
	private final String channelGroupId;
	private boolean start = true;
	private long time = 60000;

	public LoadCore(String host, long time, String channelGroupId) {
		this.host = host;
		if (time > 1000)
			this.time = time;
		this.channelGroupId = channelGroupId;
	}

	

	public String getLogFilePath() {
		return CURRENT_PATH + channelGroupId +"-"+ host + PREFIX;
	}

	public void toCheck() throws IOException {
		Transaction tr = TransactionProvider.getTransaction(channelGroupId);
		tr.setServiceName("com.anywide.dawdler.serverplug.service.CheckUpdate");
		tr.setMethod("check");
		tr.addString(host);
		XmlBean xmlb = null;
		try {
			xmlb = (XmlBean)tr.pureExecuteResult();
		} catch (Exception e) {
			logger.error("", e);
		}
		if (xmlb == null)
			throw new NullPointerException("not found host " + host + "!");
		XmlObject xmlo = new XmlObject(xmlb.getDocument());
		String filepath = getLogFilePath();
		File file = new File(filepath);
		if (!file.exists()) {
			xmlo.setFilepath(filepath);
			try {
				xmlo.setXmlfile(false);
				xmlo.saveXML();
			} catch (IOException e) {
				logger.error("", e);
			}
			initClassMap(xmlo);
		} else {
			try {
				XmlObject local = new XmlObject(filepath, false);
				if (check(local, xmlo)) {
					xmlo.setFilepath(filepath);
					try {
						xmlo.setXmlfile(false);
						xmlo.saveXML();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			} catch (DocumentException | IOException e) {
				logger.error("", e);
			}
		}
	}

	
	private static String toClassName(String checkName) {
		return checkName.replace(File.separator, ".").substring(0, checkName.lastIndexOf("."));
	}

	private void initClassMap(XmlObject xmlo) throws IOException {
		willLoad(xmlo, "api");
	}

	private void willLoad(XmlObject xmlo, String type) throws IOException {
		List<Node> beanList = xmlo.selectNodes("/hosts/host[@type='" + type + "']/item");
		String[] loadBeans = new String[beanList.size()];
		int i = 0;
		for (Object o : beanList) {
			Element ele = (Element) o;
			loadBeans[i++] = ele.attributeValue("checkname").replace(File.separator, ".");
		}
		
		loadClass(loadBeans, type.equals(TYPE_API));
	}

	private boolean willCheckAndLoad(XmlObject local, XmlObject remote, String type) throws IOException {
		boolean isApi = type.equals(TYPE_API);
		boolean remark = false;
		List<String> allClass = new ArrayList<String>();
		Set<String> needLoad = new LinkedHashSet<String>();
		// 这个for循环是为了从内存中移除 时间过期的Class对象 ,并把服务器端和客户端都有的类装入到一个list里做标记
		for (Object item : local.selectNodes("/hosts/host[@type='" + type + "']/item")) {
			Element ele = (Element) item;
			for (Object remoteItem : remote
					.selectNodes("/hosts/host[@type='" + type + "']/item[@checkname='" + ele.attributeValue("checkname") + "']")) {
				Element remoteEle = (Element) remoteItem;
				String checkName = remoteEle.attributeValue("checkname");
				String className = toClassName(checkName);
				allClass.add(checkName);
				if (!ele.attributeValue("update").equals(remoteEle.attributeValue("update"))) {
					remark = true;
					needLoad.add(className+CLASSP_REFIX);
				}
			}
		}
		String classFilePath = CURRENT_PATH;
		Set<String> loadCache = new LinkedHashSet<String>();
		for (String name : allClass) {// 循环客户端和服务器端都有的类
			for (Object item : local
					.selectNodes("/hosts/host[@type='" + type + "']/item[@checkname!='" + name + "']")) {// 查找本地文件在服务器端不存在的(去除这个names值以外的)
				Element ele = (Element) item;
				String checkName = ele.attributeValue("checkname");
				if (!allClass.contains(checkName)
						&& !loadCache.contains(checkName)) {// 如果list里面不包含并且set中也不包含
					loadCache.add(checkName);// set中添加进去
					remark = true;
					File file = new File(classFilePath + checkName);
					if (file.exists())
						file.delete();
				}
			}
			List<Node> items = remote.selectNodes("/hosts/host[@type='" + type + "']/item[@checkname!='" + name + "']");
			for (Object item : items) {
				Element ele = (Element) item;
				String checkName = ele.attributeValue("checkname");
				if (!allClass.contains(checkName)
						&& !loadCache.contains(checkName)) {
					loadCache.add(checkName);
					remark = true;
					String className = toClassName(checkName);
					needLoad.add(className+CLASSP_REFIX);
					File file = new File(classFilePath + checkName);
					if (file.exists())
						file.delete();
				}
			}
		}
		String[] loadClasses = new String[needLoad.size()];
		loadClasses = needLoad.toArray(loadClasses);
		if (loadClasses != null && loadClasses.length > 0) {
			loadClass(loadClasses, isApi);
		}
		return remark;
	}

	public void run() {
		while (start) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
			}
			try {
				toCheck();
			} catch (Exception e) {
			}
		}

	}

	public void stop() {
		this.start = false;
	}

	private boolean check(XmlObject local, XmlObject remote) throws IOException {
		return willCheckAndLoad(local, remote, TYPE_API) | willCheckAndLoad(local, remote, TYPE_COMPONENT);
	}

	private void loadClass(String[] classNames, boolean isApi) throws IOException {
		Transaction tr = TransactionProvider.getTransaction(channelGroupId);
		tr.setServiceName("com.anywide.dawdler.serverplug.service.DownloadFile");
		tr.setMethod("download");
		tr.addObject(classNames);
		RemoteFiles rfs = null;
		try {
			rfs = (RemoteFiles) tr.pureExecuteResult();
		} catch (Exception e) {
			logger.error("", e);
			return;
		}
		List<RemoteFile> list = rfs.getFiles();
		for (RemoteFile rf : list) {
			Matcher match = CLASS_PATTERN.matcher(rf.getFilename());
			if (match.find()) {
				String className = match.group(1);
				if(isApi) {
					String filePath = className.replace(".",File.separator);
					File file = new File(CURRENT_PATH + filePath + CLASSP_REFIX);
					File parentFile = new File(file.getParent());
					if (file.exists())
						file.delete();
					if (!parentFile.exists()) {
						if (!parentFile.mkdirs())
							throw new IOException("can't write file to" + parentFile.getPath());
					}
					FileOutputStream fo = null;
					try {
						fo = new FileOutputStream(file);
						fo.write(rf.getData());
						fo.flush();
					} catch (Exception e) {
						logger.error("", e);
						return;
					} finally {
						try {
							if (fo != null)
								fo.close();
						} catch (IOException e) {
							logger.error("", e);
						}
					}
				}
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("load over \t" + host + "\tmodel !");
	}

	public String getHost() {
		return host;
	}

}