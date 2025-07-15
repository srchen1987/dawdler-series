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
package club.dawdler.server.deploys;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.server.conf.ServerConfig.Scanner;
import club.dawdler.server.deploys.ServiceBase.DeployScanner;
import club.dawdler.server.deploys.loader.DawdlerDeployClassLoader;

/**
 * @author jackson.song
 * @version V1.0
 * 部署项目扫描类
 */
public class DeployClassesScanner {
	private static final Logger logger = LoggerFactory.getLogger(DeployClassesScanner.class);

	public static void findAndAddClassesInPackageByPath(Scanner scanner, DeployScanner deployScanner,
			String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes)
			throws ClassNotFoundException {
		File dir = new File(packagePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] dirFiles;
		if (scanner.emptyJar()) {
			dirFiles = dir.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
				}
			});
		} else {
			dirFiles = dir.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"))
							|| (file.getName().endsWith(".jar") && scanner.matchInJarFiles(file.getName()));
				}
			});
		}
		for (File file : dirFiles) {
			if (file.isDirectory()) {
				String fileName = file.getName();
				if (fileName.equals("classes")) {
					fileName = "";
				}
				findAndAddClassesInPackageByPath(scanner, deployScanner,
						packageName.equals("") ? fileName : (packageName + "." + file.getName()),
						file.getAbsolutePath(), recursive, classes);
			} else {
				if (file.getName().endsWith(".jar")) {
					JarFile jarFile;
					try {
						jarFile = new JarFile(file);
						findAndAddClassesInPackageInJar(scanner, deployScanner, packageName, file.getParent(), jarFile,
								recursive, classes);
					} catch (IOException e) {
						logger.error("", e);
					}
				} else {
					if (deployScanner.matchInClasses(packageName)) {
						String className = file.getName().substring(0, file.getName().length() - 6);
						if (className.equals("module-info")) {
							continue;
						}
						String loadClassName = packageName.equals("") ? className : (packageName + '.' + className);
						DawdlerDeployClassLoader classLoader = (DawdlerDeployClassLoader) Thread.currentThread()
								.getContextClassLoader();
						classes.add(classLoader.findClassForDawdler(loadClassName, true, false));
					}

				}
			}
		}
	}

	public static Set<Class<?>> getClassesInPath(Scanner scanner, DeployScanner deployScanner, File deploy)
			throws ClassNotFoundException {
		Set<Class<?>> classes = new LinkedHashSet<>();
		findAndAddClassesInPackageByPath(scanner, deployScanner, "", deploy.getPath(), true, classes);
		return classes;
	}

	public static void findAndAddClassesInPackageInJar(Scanner scanner, DeployScanner deployScanner, String packageName,
			String packageDirName, JarFile jar, final boolean recursive, Set<Class<?>> classes) {
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.charAt(0) == '/') {
				name = name.substring(1);
			}
			if (name.endsWith(".class")) {
				int idx = name.lastIndexOf('/');
				if (idx != -1) {
					packageName = name.substring(0, idx).replace('/', '.');
				}
				if (!scanner.matchInJars(packageName) && !deployScanner.matchInJars(packageName)) {
					continue;
				}
				if ((idx != -1) || recursive) {
					if (name.endsWith(".class") && !entry.isDirectory()) {
						String className;
						if (idx != -1)
							className = name.substring(packageName.length() + 1, name.length() - 6);
						else {
							className = name.substring(0, name.length() - 6);
						}
						if (className.equals("module-info")) {
							continue;
						}

						String loadClassName = idx == -1 ? className : (packageName + '.' + className);
						DawdlerDeployClassLoader classLoader = (DawdlerDeployClassLoader) Thread.currentThread()
								.getContextClassLoader();
						try {
							classes.add(classLoader.findClassForDawdler(loadClassName, true, false));
						} catch (Throwable e) {
							logger.error("", e);
						}

					}
				}
			}
		}
	}

}
