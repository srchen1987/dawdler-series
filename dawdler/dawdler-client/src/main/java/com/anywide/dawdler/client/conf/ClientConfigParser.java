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
package com.anywide.dawdler.client.conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.util.DawdlerTool;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClientConfigParser.java
 * @Description xtream映射类
 * @date 2015年03月16日
 * @email suxuan696@gmail.com
 */
public class ClientConfigParser {
	private static final Logger logger = LoggerFactory.getLogger(ClientConfigParser.class);
	private static ClientConfig config = null;

	static {
		InputStream input = null;
		try {
//			XStream schema = new XStream();
//			// clear out existing permissions and set own ones
//			schema.addPermission(NoTypePermission.NONE);
//			// allow some basics
//			schema.addPermission(NullPermission.NULL);
//			schema.addPermission(PrimitiveTypePermission.PRIMITIVES);
//			schema.allowTypeHierarchy(Collection.class);
//			// allow any type from the same package
//			schema.allowTypesByWildcard(new String[] {
//			    "com.anywide.dawdler.client.conf.*"
//			});

			input = new FileInputStream(DawdlerTool.getcurrentPath() + "client/client-conf.xml");
			XStream xstream = new XStream();
			xstream.ignoreUnknownElements();
			xstream.alias("config", ClientConfig.class);
			xstream.autodetectAnnotations(true);
			xstream.addPermission(NoTypePermission.NONE);
			xstream.addPermission(NullPermission.NULL);
			xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
			xstream.allowTypeHierarchy(Collection.class);
			xstream.allowTypesByWildcard(new String[] { "com.anywide.dawdler.client.conf.*" });
			config = (ClientConfig) xstream.fromXML(input);
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

	public static ClientConfig getClientConfig() {
		return config;
	}
}
