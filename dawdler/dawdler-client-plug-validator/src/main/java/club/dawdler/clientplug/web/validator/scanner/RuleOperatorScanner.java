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
package club.dawdler.clientplug.web.validator.scanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 验证规则扫描器
 */
public class RuleOperatorScanner {
	private static final Logger logger = LoggerFactory.getLogger(RuleOperatorScanner.class);

	public static Set<Class<?>> getAppClasses(String pack) {
		Set<Class<?>> classes = new LinkedHashSet<>();
		boolean recursive = true;
		String packageDirName = pack.replace('.', '/');
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findAndAddClassesInPackageByFile(pack, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {
					findAndAddClassesInPackageByJar(pack, packageDirName, url, recursive, classes);

				}
			}
		} catch (IOException e) {
		}
		return classes;
	}

	public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
			Set<Class<?>> classes) {
		File dir = new File(packagePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] dirFiles = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		for (File file : dirFiles) {
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
						classes);
			} else {
				String className = file.getName().substring(0, file.getName().length() - 6);
				try {
					classes.add(
							Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
				}
			}
		}
	}

	public static void findAndAddClassesInPackageByJar(String packageName, String packageDirName, URL url,
			final boolean recursive, Set<Class<?>> classes) {
		String protocol = url.getProtocol();
		if (url != null) {
			if (("jar".equals(protocol))) {
				JarFile jar;
				try {
					jar = ((JarURLConnection) url.openConnection()).getJarFile();
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						if (name.charAt(0) == '/') {
							name = name.substring(1);
						}
						if (name.startsWith(packageDirName)) {
							int idx = name.lastIndexOf('/');
							if (idx != -1) {
								packageName = name.substring(0, idx).replace('/', '.');
							}
							if ((idx != -1) || recursive) {
								if (name.endsWith(".class") && !entry.isDirectory()) {
									String className = name.substring(packageName.length() + 1, name.length() - 6);
									try {
										classes.add(Class.forName(packageName + '.' + className));
									} catch (ClassNotFoundException e) {
									}
								}
							}
						}
					}
				} catch (IOException e) {
				}
			} else if ("file".equals(protocol)) {
				String filePath;
				try {
					filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findAndAddClassesInPackageByFile(packageName, new File(filePath).getParent(), recursive, classes);
				} catch (UnsupportedEncodingException e) {
					logger.error("", e);
				}
			}
		}
	}

	public static Set<Class<?>> getLibClasses(Class<?> c) {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		if (c != null) {
			URL url = c.getResource(c.getSimpleName() + ".class");
			String pack = c.getPackage().getName();
			boolean recursive = true;
			String packageName = pack;
			String packageDirName = packageName.replace('.', '/');
			if (url != null) {
				findAndAddClassesInPackageByJar(packageName, packageDirName, url, recursive, classes);
			}
		}
		return classes;
	}
}
