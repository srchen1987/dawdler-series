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
package com.anywide.dawdler.clientplug.load.classloader;

import com.anywide.dawdler.clientplug.load.LoadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientPlugClassLoader.java
 * @Description 提供加载方法来加载远端模版类到jvm中
 * @date 2007年07月22日
 * @email suxuan696@gmail.com
 */
public class ClientPlugClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClientPlugClassLoader.class);
    private static final Map<String, Class> remoteClass = new ConcurrentHashMap<>();
    private static ClientPlugClassLoader classloader = null;
    private final List<RemoteClassLoderFire> fireList = RemoteClassLoaderFireHolder.getInstance().getRemoteClassLoaderFire();
    private URLClassLoader urlCL = null;

    private ClientPlugClassLoader(String path) {
        updateLoad(path);
    }

    synchronized public static ClientPlugClassLoader newInstance(String path) {
        if (classloader == null)
            classloader = new ClientPlugClassLoader(path);
        return classloader;
    }

    public static Class getRemoteClass(String key) {
        return remoteClass.get(key);
    }

    public void load(String host, String name, String path) {
        if (LoadListener.DEBUG)
            System.out.println("loading %%%" + host + "%%%module  \t" + path + ".class");
        try {
            Class c = urlCL.loadClass(path);
            remoteClass.put(host.trim() + "-" + name.trim(), c);
            for (RemoteClassLoderFire rf : fireList) {
                rf.onLoadFire(c);
            }
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
    }

    public void remove(String name) {
        if (LoadListener.DEBUG)
            System.out.println("remove class " + name + ".class");
        Class c = remoteClass.remove(name);
        for (RemoteClassLoderFire rf : fireList) {
            rf.onRemoveFire(c);
        }
    }

    public void updateLoad(String path) {
        URLClassLoader oldUrlCL = urlCL;
        try {
            URL url = new URL("file:" + path + "/");
            this.urlCL = ClientClassLoader.newInstance(new URL[]{url}, getClass().getClassLoader());
        } catch (MalformedURLException e) {
            logger.error("", e);
        } finally {
            try {
                if (oldUrlCL != null)
                    oldUrlCL.close();
            } catch (IOException e) {
            }
        }
    }
}
