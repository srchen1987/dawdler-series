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
package club.dawdler.clientplug.web.jwt.algorithm;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import club.dawdler.clientplug.web.jwt.exception.JwtException;

/**
 * @author jackson.song
 * @version V1.0
 * 基于HMAC的对称签名算法,支持HS256 HS384 HS512.
 */
public class HmacAlgorithm implements Algorithm {

	private static final Map<String, String> JAVA_ALGORITHMS;
	static {
		Map<String, String> m = new HashMap<>();
		m.put("HS256", "HmacSHA256");
		m.put("HS384", "HmacSHA384");
		m.put("HS512", "HmacSHA512");
		JAVA_ALGORITHMS = Collections.unmodifiableMap(m);
	}

	private final String name;
	private final String javaAlgorithm;
	private final SecretKeySpec keySpec;

	public HmacAlgorithm(String name, byte[] secret) {
		String javaAlgorithm = JAVA_ALGORITHMS.get(name);
		if (javaAlgorithm == null) {
			throw new JwtException("unsupported hmac algorithm: " + name);
		}
		if (secret == null || secret.length == 0) {
			throw new JwtException("hmac secret can not be empty for algorithm: " + name);
		}
		this.name = name;
		this.javaAlgorithm = javaAlgorithm;
		this.keySpec = new SecretKeySpec(secret, javaAlgorithm);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] sign(byte[] data) {
		try {
			Mac mac = Mac.getInstance(javaAlgorithm);
			mac.init(keySpec);
			return mac.doFinal(data);
		} catch (Exception e) {
			throw new JwtException("hmac sign error", e);
		}
	}

	@Override
	public boolean verify(byte[] data, byte[] signature) {
		if (signature == null) {
			return false;
		}
		byte[] expected = sign(data);
		return MessageDigest.isEqual(expected, signature);
	}

}
