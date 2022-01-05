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
 * @Title DawdlerTool.java
 * @Description 常用工具类
 * @date 2007年7月03日
 * @email suxuan696@gmail.com
 */
public class DawdlerTool {

	public static String getcurrentPath() {
		try {
			return URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("").getPath(), "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		return Thread.currentThread().getContextClassLoader().getResource("").getPath().replace("%20", " ");
	}

	public static URL getcurrentURL() {
		return Thread.currentThread().getContextClassLoader().getResource("");
	}

	public static String getEnv(String key) {
		return System.getenv(key);
	}
	
	public static String getProperty(String key) {
		return System.getProperty(key);
	}

	public static String fnameToUpper(String fieldname) {
		char c = fieldname.charAt(0);
		c = (char) (c - 32);
		fieldname = c + fieldname.substring(1, fieldname.length());
		return fieldname;
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

	static public String generateDigest(String idPassword) throws NoSuchAlgorithmException {
		String parts[] = idPassword.split(":", 2);
		byte digest[] = MessageDigest.getInstance("SHA1").digest(idPassword.getBytes());
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
		DecimalFormat kbFormat = new DecimalFormat("#0.00");
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		System.out.println("OS arch: " + operatingSystemMXBean.getArch());
		System.out.println("OS availableProcessors: " + operatingSystemMXBean.getAvailableProcessors());
		System.out.println("OS name: " + operatingSystemMXBean.getName());
		System.out.println("OS version: " + operatingSystemMXBean.getVersion());
		System.out.println("JAVA version: "+System.getProperty("java.version"));
		Runtime runtime = Runtime.getRuntime();
		double freeMemory = (double) runtime.freeMemory();
		double totalMemory = (double) runtime.totalMemory();
		System.out.println("Jvm totalMemory: " + totalMemory + "(" + kbFormat.format(totalMemory / 1024) + "K)");
		System.out.println("Jvm freeMemory: " + freeMemory + "(" + kbFormat.format(freeMemory / 1024) + "K)");
		MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
		System.out.println("Heap Memory Usage:");
		System.out.println(memorymbean.getHeapMemoryUsage());
		System.out.println("Non-Heap Memory Usage:");
		System.out.println(memorymbean.getNonHeapMemoryUsage());
		List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		System.out.println("Java options:");
		System.out.println(inputArguments);
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		System.out.println("ClassPath: " + runtimeMXBean.getClassPath());
		System.out.println("LibraryPath: " + runtimeMXBean.getLibraryPath());
	}
}
