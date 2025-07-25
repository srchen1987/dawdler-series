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
package club.dawdler.clientplug.load;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import club.dawdler.client.Transaction;
import club.dawdler.client.TransactionProvider;
import club.dawdler.clientplug.web.classloader.ClientPlugClassLoader;
import club.dawdler.serverplug.bean.XmlBean;
import club.dawdler.serverplug.load.bean.RemoteFiles;
import club.dawdler.serverplug.load.bean.RemoteFiles.RemoteFile;
import club.dawdler.util.DawdlerTool;
import club.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * 加载服务端类模版
 */
public class LoadCore implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(LoadCore.class);
	private static final Pattern CLASS_PATTERN = Pattern.compile("(.*)\\.class$");
	private static final String CLASS_REFIX = ".class";
	private XmlObject xmlObjectPreCache;

	private final String host;
	private final String channelGroupId;
	private volatile boolean start = true;
	private long time = 60000;
	private ClientPlugClassLoader classLoader = null;

	public LoadCore(String host, long time, String channelGroupId, ClientPlugClassLoader classLoader) {
		this.classLoader = classLoader;
		this.host = host;
		if (time > 1000) {
			this.time = time;
		}
		this.channelGroupId = channelGroupId;
	}

	public void toCheck() throws Throwable {
		Transaction tr = TransactionProvider.getTransaction(channelGroupId);
		tr.setServiceName("club.dawdler.serverplug.service.CheckUpdate");
		tr.setMethod("check");
		tr.addString(host);
		XmlBean xmlb = null;
		try {
			xmlb = (XmlBean) tr.pureExecuteResult();
		} catch (Exception e) {
			logger.error("", e);
		}
		if (xmlb == null) {
			throw new NullPointerException("not found host " + host + "!");
		}
		XmlObject xmlo = new XmlObject(xmlb.getDocument());
		if (xmlObjectPreCache == null) {
			xmlObjectPreCache = xmlo;
			initClassMap(xmlo);
		} else {
			try {
				XmlObject local = xmlObjectPreCache;
				if (check(local, xmlo)) {
					xmlObjectPreCache = xmlo;
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	private static String toClassName(String checkName) {
		return checkName.replace(File.separator, ".").substring(0, checkName.lastIndexOf("."));
	}

	private void initClassMap(XmlObject xmlo) throws Throwable {
		willLoad(xmlo);
	}

	private void willLoad(XmlObject xmlo) throws Throwable {
		List<Node> beanList = xmlo.selectNodes("/hosts/host/item");
		String[] loadBeans = new String[beanList.size()];
		int i = 0;
		for (Object o : beanList) {
			Element ele = (Element) o;
			loadBeans[i++] = ele.getAttribute("checkname").replace(File.separator, ".");
		}
		loadClass(loadBeans);
	}

	private boolean willCheckAndLoad(XmlObject local, XmlObject remote) throws Throwable {
		boolean remark = false;
		List<String> allClass = new ArrayList<String>();
		List<String> allLocalClass = new ArrayList<String>();
		Set<String> needLoad = new LinkedHashSet<String>();

		List<Node> localItems = local.selectNodes("/hosts/host/item");
		if (localItems.isEmpty()) {
			List<Node> remoteItems = remote.selectNodes("/hosts/host/item");
			remark = !remoteItems.isEmpty();
			for (Object remoteItem : remoteItems) {
				Element remoteEle = (Element) remoteItem;
				String checkName = remoteEle.getAttribute("checkname");
				String className = toClassName(checkName);
				needLoad.add(className + CLASS_REFIX);
			}
		}

		for (Node item : localItems) {
			Element ele = (Element) item;
			String localCheckName = ele.getAttribute("checkname");
			allLocalClass.add(toClassName(localCheckName));
			for (Object remoteItem : remote.selectNodes("/hosts/host/item[@checkname='" + localCheckName + "']")) {
				Element remoteEle = (Element) remoteItem;
				String checkName = remoteEle.getAttribute("checkname");
				String className = toClassName(checkName);
				allClass.add(checkName);
				if (!ele.getAttribute("update").equals(remoteEle.getAttribute("update"))) {
					remark = true;
					needLoad.add(className + CLASS_REFIX);
					if (ClientPlugClassLoader.getRemoteClass((host + "-" + className)) != null) {
						this.classLoader.remove(host + "-" + className);
					}
				}
			}
		}
		Set<String> loadCache = new LinkedHashSet<String>();
		if (allClass.isEmpty()) {
			allClass = allLocalClass;
		}
		for (String name : allClass) {// 循环客户端和服务器端都有的类
			for (Object item : local.selectNodes("/hosts/host/item[@checkname!='" + name + "']")) {// 查找本地文件在服务器端不存在的(去除这个names值以外的)
				Element ele = (Element) item;
				String checkName = ele.getAttribute("checkname");
				String className = toClassName(checkName);
				if (!allClass.contains(checkName) && !loadCache.contains(checkName)) {// 如果list里面不包含并且set中也不包含
					loadCache.add(checkName);// set中添加进去
					remark = true;
					if (ClientPlugClassLoader.getRemoteClass((host + "-" + className)) != null) {
						this.classLoader.remove(host + "-" + className);
					}
				}
			}
			List<Node> items = remote.selectNodes("/hosts/host/item[@checkname!='" + name + "']");
			for (Object item : items) {
				Element ele = (Element) item;
				String checkName = ele.getAttribute("checkname");
				if (!allClass.contains(checkName) && !loadCache.contains(checkName)) {
					loadCache.add(checkName);
					remark = true;
					String className = toClassName(checkName);
					needLoad.add(className + CLASS_REFIX);
					if (ClientPlugClassLoader.getRemoteClass((host + "-" + className)) != null) {
						this.classLoader.remove(host + "-" + className);
					}
				}
			}
		}
		String[] loadClasses = new String[needLoad.size()];
		loadClasses = needLoad.toArray(loadClasses);
		if (loadClasses != null && loadClasses.length > 0) {
			classLoader.updateLoad(DawdlerTool.getCurrentPath());
			loadClass(loadClasses);
		}

		allClass.clear();
		allLocalClass.clear();
		needLoad.clear();

		return remark;
	}

	public void run() {
		while (start) {
			try {
				Thread.sleep(time);
				try {
					toCheck();
				} catch (Throwable e) {
					logger.error("", e);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	public void stop() {
		this.start = false;
	}

	private boolean check(XmlObject local, XmlObject remote) throws Throwable {
		return willCheckAndLoad(local, remote);
	}

	private void loadClass(String[] classNames) throws Throwable {
		Transaction tr = TransactionProvider.getTransaction(channelGroupId);
		tr.setServiceName("club.dawdler.serverplug.service.DownloadFile");
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
				this.classLoader.load(host, className, rf.getData());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("load over \t" + host + "\tmodel !");
		}
	}

	public String getHost() {
		return host;
	}

}
