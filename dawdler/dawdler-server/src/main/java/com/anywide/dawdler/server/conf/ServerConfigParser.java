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
package com.anywide.dawdler.server.conf;

import com.anywide.dawdler.util.DawdlerTool;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServerConfigParser.java
 * @Description 服务器配置解析类 通过xstream映射
 * @date 2015年04月04日
 * @email suxuan696@gmail.com
 */
public class ServerConfigParser {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfigParser.class);
    private static ServerConfig config = null;

    static {
        InputStream input = null;
        try {
            input = new FileInputStream(DawdlerTool.getcurrentPath() + "../conf/server-conf.xml");
            XStream xstream = new XStream(new DomDriver());
            xstream.alias("config", ServerConfig.class);
            xstream.autodetectAnnotations(true);
            xstream.ignoreUnknownElements();
            xstream.addPermission(NoTypePermission.NONE);
            xstream.addPermission(AnyTypePermission.ANY);
            xstream.addPermission(NullPermission.NULL);
            xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
            xstream.allowTypeHierarchy(Collection.class);
            xstream.alias("users", Map.class);
            xstream.alias("list", Map.Entry.class);
            xstream.allowTypesByWildcard(new String[]{"com.anywide.dawdler.server.conf.*"});
            config = (ServerConfig) xstream.fromXML(input);
        } catch (FileNotFoundException e) {
            logger.error("", e);
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
        }

    }

    public static ServerConfig getServerConfig() {
        return config;
    }
}
