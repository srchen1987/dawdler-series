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

import com.anywide.dawdler.client.Transaction;
import com.anywide.dawdler.client.TransactionProvider;
import com.anywide.dawdler.clientplug.load.classloader.ClientPlugClassLoader;
import com.anywide.dawdler.serverplug.bean.XmlBean;
import com.anywide.dawdler.serverplug.load.bean.RemoteFiles;
import com.anywide.dawdler.serverplug.load.bean.RemoteFiles.RemoteFile;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jackson.song
 * @version V1.0
 * @Title LoadCore.java
 * @Description 加载服务端类模版
 * @date 2007年09月05日
 * @email suxuan696@gmail.com
 */
public class LoadCore implements Runnable {
    private static final String PREFIX = ".dat";
    private static final Logger logger = LoggerFactory.getLogger(LoadCore.class);
    private static final Pattern p = Pattern.compile("(.*)\\.class$");
    private static final String currentpath;
    private static String path;

    static {
        XmlObject xmlo = ClientConfig.getInstance().getXml();
        Element ele = xmlo.selectSingleNode("/config/load-url");
        currentpath = DawdlerTool.getcurrentPath();
        String tmpPath = System.getProperty("user.home");
        if (tmpPath != null) {
            tmpPath = tmpPath + File.separator + ".load" + File.separator + UUID.randomUUID().toString();
            File file = new File(tmpPath);
            if (!file.exists()) {
                if (!file.mkdirs())
                    tmpPath = currentpath;
            }
        } else
            tmpPath = currentpath;
        path = tmpPath;
        if (ele != null) {
            if (!ele.getTextTrim().equals("")) {
                File f = new File(ele.getTextTrim());
                if (!f.isDirectory()) {
                    if (f.mkdirs()) {
                        path = f.getAbsolutePath();
                    }
                } else {
                    path = f.getAbsolutePath();
                }
            }
        }
    }

    private final String host;
    private final String channelGroupId;
    private boolean start = true;
    private long time = 600000;
    private ClientPlugClassLoader cl = null;

    public LoadCore(String host, long time, String channelGroupId) {
        cl = ClientPlugClassLoader.newInstance(path);
        this.host = host;
        if (time > 1000)
            this.time = time;
        this.channelGroupId = channelGroupId;
    }

    private final static String classNameToFilePath(String classname) {
        int tag = classname.lastIndexOf(".");
        String filepath = classname.substring(0, tag).replace(".", File.separator);
        String filetype = classname.substring(tag);
        return filepath + filetype;
    }

    public String getLogFilePath() {
        return path + File.separator + channelGroupId + host + PREFIX;
    }

    public void toCheck() throws IOException {
        Transaction tr = TransactionProvider.getTransaction(channelGroupId);
        tr.setServiceName("com.anywide.dawdler.serverplug.service.CheckUpdate");
        tr.setMethod("check");
        tr.addString(host);
        XmlBean xmlb = null;
        try {
            xmlb = (XmlBean) tr.pureExecuteResult();
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
                if (check(new XmlObject(filepath, false), xmlo)) {
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

    public void initBeans() {
        cl.updateLoad(path);
        try {
            String filepath = getLogFilePath();
            XmlObject xmlo = new XmlObject(filepath, false);
            for (Object o : xmlo.selectNodes("/hosts/host[@type='controller']/item")) {
                Element ele = (Element) o;
                if (ClientPlugClassLoader.getRemoteClass((host + "-" + ele.attributeValue("name"))) == null) {
                    String tempname = classNameToFilePath(ele.getText());
                    tempname = tempname.substring(0, tempname.lastIndexOf("."));
                    cl.load(host, ele.attributeValue("name"), tempname.replace(File.separator, "."));
                }
            }
        } catch (DocumentException | IOException e) {
            logger.error("", e);
        }
    }

    private void initClassMap(XmlObject xmlo) throws IOException {
        willLoad(xmlo, "bean");
        willLoad(xmlo, "controller");
    }

    private void willLoad(XmlObject xmlo, String type) throws IOException {
        List beanList = xmlo.selectNodes("/hosts/host[@type='" + type + "']/item");
        String[] tem = new String[beanList.size()];
        int i = 0;
        for (Object o : beanList) {
            Element ele = (Element) o;
            tem[i++] = ele.getTextTrim();
        }
        loadClass(tem, type.equals("bean"));
    }

    private boolean willCheckAndLoad(XmlObject local, XmlObject remote, String type) throws IOException {
        boolean isbean = type.equals("bean");
        boolean remark = false;
        List<String> list = new ArrayList<String>();
        Set<String> set = new HashSet<String>();
        // 这个for循环是为了从内存中移除 时间过期的Class对象 ,并把服务器端和客户端都有的类装入到一个list里做标记
        for (Object item : local.selectNodes("/hosts/host[@type='" + type + "']/item")) {
            Element ele = (Element) item;
            String checkname = ele.attributeValue("checkname");
            for (Object item2 : remote
                    .selectNodes("/hosts/host[@type='" + type + "']/item[@checkname='" + checkname + "']")) {
                Element ele2 = (Element) item2;
                String ele2checkname = ele2.attributeValue("checkname");
                list.add(ele2checkname);
                if (!ele.attributeValue("update").equals(ele2.attributeValue("update"))) {
                    remark = true;
                    set.add(ele2.getText());
                    if (!isbean && ClientPlugClassLoader.getRemoteClass((host + "-" + ele2.attributeValue("name"))) != null)
                        cl.remove(host + "-" + ele2.attributeValue("name"));
                }
            }
        }
        String classFilePath = (isbean ? currentpath : (path + File.separator));
        Set<String> temset = new HashSet<String>();
        for (String names : list) {// 循环客户端和服务器端都有的类
            for (Object item : local.selectNodes("/hosts/host[@type='" + type + "']/item[@checkname!='" + names + "']")) {// 查找本地文件在服务器端不存在的(去除这个names值以外的)
                Element ele = (Element) item;
                if (!list.contains(ele.attributeValue("checkname"))
                        && !temset.contains(ele.attributeValue("checkname"))) {// 如果list里面不包含并且set中也不包含
                    temset.add(ele.attributeValue("checkname"));// set中添加进去
                    remark = true;
                    File file = new File(classFilePath + classNameToFilePath(ele.getText()));
                    if (file.exists())
                        file.delete();
                    if (!isbean && ClientPlugClassLoader.getRemoteClass((host + "-" + ele.attributeValue("name"))) != null)
                        cl.remove(host + "-" + ele.attributeValue("name"));
                }
            }
            List items = remote.selectNodes("/hosts/host[@type='" + type + "']/item[@checkname!='" + names + "']");
            for (Object item : items) {
                Element ele = (Element) item;
                if (!list.contains(ele.attributeValue("checkname"))
                        && !temset.contains(ele.attributeValue("checkname"))) {
                    temset.add(ele.attributeValue("checkname"));
                    remark = true;
                    set.add(ele.getText());
                    File file = new File(classFilePath + classNameToFilePath(ele.getText()));
                    if (file.exists())
                        file.delete();
                    if (!isbean && ClientPlugClassLoader.getRemoteClass((host + "-" + ele.attributeValue("name"))) != null)
                        cl.remove(host + "-" + ele.attributeValue("name"));
                }
            }
        }
        String[] tem = new String[set.size()];
        tem = set.toArray(tem);
        if (tem != null && tem.length > 0)
            loadClass(tem, isbean);
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
        return willCheckAndLoad(local, remote, "bean") | willCheckAndLoad(local, remote, "controller");
    }

    private void loadClass(String[] tem, boolean isbean) throws IOException {
        if (!isbean)
            cl.updateLoad(path);
        Transaction tr = TransactionProvider.getTransaction(channelGroupId);
        tr.setServiceName("com.anywide.dawdler.serverplug.service.DownloadFile");
        tr.setMethod("download");
        tr.addObject(tem);
        RemoteFiles rfs = null;
        try {
            rfs = (RemoteFiles) tr.pureExecuteResult();
        } catch (Exception e) {
            logger.error("", e);
            return;
        }
        List<RemoteFile> list = rfs.getFiles();
        for (RemoteFile rf : list) {
            Matcher match = p.matcher(rf.getFilename());
            if (match.find()) {
                String classpath = match.group(1);
                String temname = classpath.replace(".", File.separator);
                File file = new File((isbean ? currentpath : (path + File.separator)) + temname + ".class");
                File temfile = new File(file.getParent());
                if (file.exists())
                    file.delete();
                if (!temfile.exists()) {
                    if (!temfile.mkdirs())
                        throw new IOException("can't write file to" + temfile.getPath());
                }
                FileOutputStream fo = null;
                try {
                    fo = new FileOutputStream(file);
                    fo.write(isbean ? rf.getData() : rf.getRemoteClassData());
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


                if (!isbean)
                    cl.load(host, classpath.substring(classpath.lastIndexOf(".") + 1).toLowerCase(),
                            classpath);
            }
        }
        if (LoadListener.DEBUG)
            System.out.println("load over \t" + host + "\tmodel !");
    }

    public String getHost() {
        return host;
    }

}
