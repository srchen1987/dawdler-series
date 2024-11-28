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
package com.anywide.dawdler.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.List;

/**
 * @author jackson.song
 * @version V1.0
 * 常用工具类
 */
public class DawdlerTool {

	public static String getCurrentPath() {
		try {
			return URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		return Thread.currentThread().getContextClassLoader().getResource("").getPath().replace("%20", " ");
	}

	public static URL getCurrentURL() {
		return Thread.currentThread().getContextClassLoader().getResource("");
	}

	public static InputStream getResourceFromClassPath(String prefix, String suffix) {
		String activeProfile = System.getProperty("dawdler.profiles.active");
		String configPath = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + suffix;
		InputStream input = null;
		if (startClass != null) {
			if (!configPath.startsWith("/")) {
				configPath = "/" + configPath;
			}
			input = startClass.getResourceAsStream(configPath);
			if (input == null && activeProfile != null) {
				try {
					if (!prefix.startsWith("/")) {
						prefix = "/" + prefix;
					}
					input = startClass.getResourceAsStream(prefix.concat(suffix));
				} catch (Exception e) {
				}
			}
		} else {
			input = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath);
			if (input == null) {
				try {
					input = Thread.currentThread().getContextClassLoader().getResourceAsStream(prefix.concat(suffix));
				} catch (Exception e) {
				}
			}
		}
		return input;
	}

	public static URL getResourceURLFromClassPath(String prefix, String suffix) {
		String activeProfile = System.getProperty("dawdler.profiles.active");
		String configPath = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + suffix;
		URL url = null;
		if (startClass != null) {
			if (!configPath.startsWith("/")) {
				configPath = "/" + configPath;
			}
			url = startClass.getResource(configPath);
			if (url == null && activeProfile != null) {
				try {
					if (!prefix.startsWith("/")) {
						prefix = "/" + prefix;
					}
					url = startClass.getResource(prefix.concat(suffix));
				} catch (Exception e) {
				}
			}
		} else {
			url = Thread.currentThread().getContextClassLoader().getResource(configPath);
			if (url == null) {
				try {
					url = Thread.currentThread().getContextClassLoader().getResource(prefix.concat(suffix));
				} catch (Exception e) {
				}
			}
		}
		return url;
	}

	public static URL getResourceURLFromClassPath(String resourcePath) {
		if (resourcePath == null) {
			return null;
		}
		if (!resourcePath.startsWith("/")) {
			resourcePath = "/" + resourcePath;
		}
		URL url = null;
		if (startClass != null) {
			url = startClass.getResource(resourcePath);
		} else {
			url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
		}
		return url;
	}

	public static InputStream getResourceFromClassPath(String resourcePath) {
		InputStream input = null;
		if (startClass != null) {
			if (!resourcePath.startsWith("/")) {
				resourcePath = "/" + resourcePath;
			}
			input = startClass.getResourceAsStream(resourcePath);
		} else {
			input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		}
		return input;
	}

	public static String getEnv(String key) {
		return System.getenv(key);
	}

	public static String getProperty(String key) {
		return System.getProperty(key);
	}

	public static String fnameToUpper(String fieldName) {
		char c = fieldName.charAt(0);
		c = (char) (c - 32);
		fieldName = c + fieldName.substring(1, fieldName.length());
		return fieldName;
	}

	public static String getSha1(String str) {
		if (null == str || 0 == str.length()) {
			return null;
		}
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			mdTemp.update(str.getBytes("UTF-8"));

			byte[] md = mdTemp.digest();
			int j = md.length;
			char[] buf = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

	public static String generateDigest(String idPassword) throws NoSuchAlgorithmException {
		String[] parts = idPassword.split(":", 2);
		byte[] digest = MessageDigest.getInstance("SHA1").digest(idPassword.getBytes());
		return parts[0] + ":" + new String(Base64.getEncoder().encode(digest));
	}

	public static String memoryStatistic() {
		Runtime runtime = Runtime.getRuntime();
		double freeMemory = (double) runtime.freeMemory() / (1024 * 1024);
		double maxMemory = (double) runtime.maxMemory() / (1024 * 1024);
		double totalMemory = (double) runtime.totalMemory() / (1024 * 1024);
		double usedMemory = totalMemory - freeMemory;
		double percentFree = ((maxMemory - usedMemory) / maxMemory) * 100.0;
		double percentUsed = 100 - percentFree;
		DecimalFormat mbFormat = new DecimalFormat("#0.00");
		DecimalFormat percentFormat = new DecimalFormat("#0.0");
		StringBuilder sb = new StringBuilder();
		sb.append("memory: " + mbFormat.format(usedMemory)).append("MB of ").append(mbFormat.format(maxMemory))
				.append(" MB (").append(percentFormat.format(percentUsed)).append("%) used");
		return sb.toString();
	}

	public static void printServerBaseInformation() {
		System.out.println("Welcome to use dawdler!\n");
		String logoAscii = "  _____              __          __  _____    _        ______   _____  \r\n"
				+ " |  __ \\      /\\     \\ \\        / / |  __ \\  | |      |  ____| |  __ \\ \r\n"
				+ " | |  | |    /  \\     \\ \\  /\\  / /  | |  | | | |      | |__    | |__) |\r\n"
				+ " | |  | |   / /\\ \\     \\ \\/  \\/ /   | |  | | | |      |  __|   |  _  / \r\n"
				+ " | |__| |  / ____ \\     \\  /\\  /    | |__| | | |____  | |____  | | \\ \\ \r\n"
				+ " |_____/  /_/    \\_\\     \\/  \\/     |_____/  |______| |______| |_|  \\_\\\r\n"
				+ "                                                                       \r\n"
				+ "                                                                       ";
		System.out.println(logoAscii);
		DecimalFormat kbFormat = new DecimalFormat("#0.00");
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		System.out.println("OS arch: " + operatingSystemMXBean.getArch());
		System.out.println("OS availableProcessors: " + operatingSystemMXBean.getAvailableProcessors());
		System.out.println("OS name: " + operatingSystemMXBean.getName());
		System.out.println("OS version: " + operatingSystemMXBean.getVersion());
		System.out.println("JAVA version: " + System.getProperty("java.version"));
		Runtime runtime = Runtime.getRuntime();
		double freeMemory = (double) runtime.freeMemory();
		double totalMemory = (double) runtime.totalMemory();
		System.out.println("Jvm totalMemory: " + totalMemory + "(" + kbFormat.format(totalMemory / 1024) + "K)");
		System.out.println("Jvm freeMemory: " + freeMemory + "(" + kbFormat.format(freeMemory / 1024) + "K)");
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		System.out.println("Heap Memory Usage:");
		System.out.println(memoryMXBean.getHeapMemoryUsage());
		System.out.println("Non-Heap Memory Usage:");
		System.out.println(memoryMXBean.getNonHeapMemoryUsage());
		List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		System.out.println("Java options:");
		System.out.println(inputArguments);
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		System.out.println("ClassPath: " + runtimeMXBean.getClassPath());
		System.out.println("LibraryPath: " + runtimeMXBean.getLibraryPath());
	}

	private static Class<?> startClass;

	public static Class<?> getStartClass() {
		return startClass;
	}
}
