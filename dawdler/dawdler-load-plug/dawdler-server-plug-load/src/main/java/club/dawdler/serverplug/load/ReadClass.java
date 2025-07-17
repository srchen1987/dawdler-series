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
package club.dawdler.serverplug.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import club.dawdler.core.scan.DawdlerComponentScanner;
import club.dawdler.server.context.DawdlerContext;
import club.dawdler.serverplug.load.bean.RemoteFiles;
import club.dawdler.serverplug.load.bean.RemoteFiles.RemoteFile;
import club.dawdler.util.DawdlerTool;
import club.dawdler.util.XmlObject;
import club.dawdler.util.spring.antpath.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * 读取服务端类模版
 */
public class ReadClass {
	private static final Logger logger = LoggerFactory.getLogger(ReadClass.class);
	private static final Pattern classPattern = Pattern.compile("(.*)\\.class$");

	public static XmlObject read(String host) {
		InputStream input = null;
		try {
			input = DawdlerTool.getResourceFromClassPath(
					DawdlerContext.getDawdlerContext().getServicesConfig().getRemoteLoad(), "");
			XmlObject remoteLoadXml = new XmlObject(input);
			List<Node> hosts = remoteLoadXml.selectNodes("/hosts/host[@name='" + host + "']/package");
			if (hosts == null || hosts.isEmpty()) {
				return null;
			}
			XmlObject xmlo = new XmlObject();
			xmlo.createRoot("hosts");
			Element root = xmlo.getRoot();
			for (Object hostObj : hosts) {
				Element hostEle = (Element) hostObj;
				Resource[] resources = DawdlerComponentScanner.getClasses(hostEle.getTextContent().trim());
				if (resources != null) {
					Document document = root.getOwnerDocument();
					Element hostElement = document.createElement("host");
					root.appendChild(hostElement);
					for (Resource rs : resources) {
						Matcher match = classPattern.matcher(rs.getFilename());
						if (match.find()) {
							Element itemElement = document.createElement("item");
							hostElement.appendChild(itemElement);
							InputStream in = rs.getInputStream();
							ClassReader classReader = new ClassReader(in);
							ClassNode classNode = new ClassNode();
							classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
							in.close();
							itemElement.setAttribute("checkname", classNode.name.concat(".class"));
							itemElement.setAttribute("update", "" + rs.lastModified());
						}
					}
				}
			}
			return xmlo;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static RemoteFiles readRemoteFiles(String[] filenames) throws IOException {
		RemoteFiles rfs = new RemoteFiles();
		List<RemoteFile> files = new ArrayList<>();
		for (String name : filenames) {
			Matcher match = classPattern.matcher(name);
			if (match.find()) {
				Resource resource = DawdlerComponentScanner.getClass(match.group(1));
				InputStream in;
				if (resource != null) {
					try {
						in = resource.getInputStream();
					} catch (IOException e) {
						logger.error("", e);
						throw e;
					}
					RemoteFile rf = new RemoteFiles().new RemoteFile();
					rf.setFilename(name);
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
							if (in != null) {
								in.close();
							}
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
