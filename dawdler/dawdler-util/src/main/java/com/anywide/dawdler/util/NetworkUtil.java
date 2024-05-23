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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author jackson.song
 * @version V1.0
 * 网络接口的工具类
 */
public class NetworkUtil {
	public static final String IPV6 = "::";
	public static final String IPV4 = "0.0.0.0";

	/**
	 * @return List<NetworkInterface>
	 * @throws SocketException
	 * 获取可用网络接口
	 * @author jackson.song
	 */
	public static List<NetworkInterface> selectActiveNetworkInterfaces() throws SocketException {
		List<NetworkInterface> interfacesList = new ArrayList<>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface nif = interfaces.nextElement();
			if (nif == null || nif.isLoopback() || nif.isVirtual() || !nif.isUp()) {
				continue;
			}
			interfacesList.add(nif);
		}
		return interfacesList;
	}

	public static String getInetAddress(String address) throws IOException {
		if (address != null && (!address.equals(IPV4) && !address.equals(IPV6))) {
			return address;
		}
		List<NetworkInterface> interfacesList = selectActiveNetworkInterfaces();
		for (NetworkInterface networkInterface : interfacesList) {
			Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
			while (inetAddress.hasMoreElements()) {
				InetAddress add = inetAddress.nextElement();
				if (address == null) {
					if (add.isReachable(200)) {
						return add.getHostAddress();
					}
				} else if (address.equals(IPV6) && !(add instanceof Inet6Address)) {
					continue;
				} else if (address.equals(IPV4) && !(add instanceof Inet4Address)) {
					continue;
				}
				if (add.isReachable(200)) {
					return add.getHostAddress();
				}
			}
		}
		return null;
	}

}
