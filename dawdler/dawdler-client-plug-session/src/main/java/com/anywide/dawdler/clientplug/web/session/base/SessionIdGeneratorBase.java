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
package com.anywide.dawdler.clientplug.web.session.base;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author jackson.song
 * @version V1.0
 * @Title SessionIdGeneratorBase.java
 * @Description SessionId生成者 抽象出一些通用方法 直接copy tomcat代码
 * @date 2016年6月16日
 * @email suxuan696@gmail.com
 */
public abstract class SessionIdGeneratorBase implements SessionIdGenerator {
	private final Queue<SecureRandom> randoms = new ConcurrentLinkedQueue();
	private String secureRandomClass = null;
	private String secureRandomAlgorithm = "SHA1PRNG";
	private String secureRandomProvider = null;
	private String jvmRoute = "";
	private int sessionIdLength = 16;

	public String getSecureRandomClass() {
		return secureRandomClass;
	}

	public void setSecureRandomClass(String secureRandomClass) {
		this.secureRandomClass = secureRandomClass;
	}

	public String getSecureRandomAlgorithm() {
		return secureRandomAlgorithm;
	}

	public void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
		this.secureRandomAlgorithm = secureRandomAlgorithm;
	}

	public String getSecureRandomProvider() {
		return secureRandomProvider;
	}

	public void setSecureRandomProvider(String secureRandomProvider) {
		this.secureRandomProvider = secureRandomProvider;
	}

	@Override
	public String getJvmRoute() {
		return jvmRoute;
	}

	@Override
	public void setJvmRoute(String jvmRoute) {
		this.jvmRoute = jvmRoute;
	}

	@Override
	public int getSessionIdLength() {
		return sessionIdLength;
	}

	@Override
	public void setSessionIdLength(int sessionIdLength) {
		this.sessionIdLength = sessionIdLength;
	}

	@Override
	public String generateSessionId() {
		return generateSessionId(jvmRoute);
	}

	protected void getRandomBytes(byte[] bytes) {

		SecureRandom random = randoms.poll();
		if (random == null) {
			random = createSecureRandom();
		}
		random.nextBytes(bytes);
		randoms.add(random);
	}

	private SecureRandom createSecureRandom() {

		SecureRandom result = null;

		if (secureRandomClass != null) {
			try {
				// Construct and seed a new random number generator
				Class<?> clazz = Class.forName(secureRandomClass);
				result = (SecureRandom) clazz.newInstance();
			} catch (Exception e) {
			}
		}

		boolean error = false;
		if (result == null) {
			// No secureRandomClass or creation failed. Use SecureRandom.
			try {
				if (secureRandomProvider != null && secureRandomProvider.length() > 0) {
					result = SecureRandom.getInstance(secureRandomAlgorithm, secureRandomProvider);
				} else if (secureRandomAlgorithm != null && secureRandomAlgorithm.length() > 0) {
					result = SecureRandom.getInstance(secureRandomAlgorithm);
				}
			} catch (NoSuchAlgorithmException e) {
				error = true;
			} catch (NoSuchProviderException e) {
				error = true;
			}
		}

		if (result == null && error) {
			try {
				result = SecureRandom.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException e) {
			}
		}

		if (result == null) {
			result = new SecureRandom();
		}

		result.nextInt();
		return result;
	}

}
