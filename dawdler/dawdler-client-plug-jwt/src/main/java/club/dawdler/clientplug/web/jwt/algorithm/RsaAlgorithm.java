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

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import club.dawdler.clientplug.web.jwt.exception.JwtException;

/**
 * @author jackson.song
 * @version V1.0
 * 基于RSA的非对称签名算法,支持RS256 RS384 RS512.私钥用于签发,公钥用于验签.
 */
public class RsaAlgorithm implements Algorithm {

	private static final Map<String, String> JAVA_ALGORITHMS;
	static {
		Map<String, String> m = new HashMap<>();
		m.put("RS256", "SHA256withRSA");
		m.put("RS384", "SHA384withRSA");
		m.put("RS512", "SHA512withRSA");
		JAVA_ALGORITHMS = Collections.unmodifiableMap(m);
	}

	private final String name;
	private final String javaAlgorithm;
	private final PrivateKey privateKey;
	private final PublicKey publicKey;

	public RsaAlgorithm(String name, PrivateKey privateKey, PublicKey publicKey) {
		String javaAlgorithm = JAVA_ALGORITHMS.get(name);
		if (javaAlgorithm == null) {
			throw new JwtException("unsupported rsa algorithm: " + name);
		}
		if (privateKey == null && publicKey == null) {
			throw new JwtException("rsa algorithm need at least a privateKey or publicKey");
		}
		this.name = name;
		this.javaAlgorithm = javaAlgorithm;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] sign(byte[] data) {
		if (privateKey == null) {
			throw new JwtException("no rsa private key configured, can not sign token");
		}
		try {
			Signature signature = Signature.getInstance(javaAlgorithm);
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		} catch (Exception e) {
			throw new JwtException("rsa sign error", e);
		}
	}

	@Override
	public boolean verify(byte[] data, byte[] signature) {
		if (publicKey == null) {
			throw new JwtException("no rsa public key configured, can not verify token");
		}
		if (signature == null) {
			return false;
		}
		try {
			Signature sig = Signature.getInstance(javaAlgorithm);
			sig.initVerify(publicKey);
			sig.update(data);
			return sig.verify(signature);
		} catch (Exception e) {
			throw new JwtException("rsa verify error", e);
		}
	}

	public static PrivateKey readPkcs8PrivateKey(String base64OrPem) {
		byte[] der = decodePemOrBase64(base64OrPem);
		try {
			return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
		} catch (Exception e) {
			throw new JwtException("read rsa pkcs8 private key error", e);
		}
	}

	public static PublicKey readX509PublicKey(String base64OrPem) {
		byte[] der = decodePemOrBase64(base64OrPem);
		try {
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
		} catch (Exception e) {
			throw new JwtException("read rsa x509 public key error", e);
		}
	}

	private static byte[] decodePemOrBase64(String base64OrPem) {
		if (base64OrPem == null || base64OrPem.trim().isEmpty()) {
			throw new JwtException("key content can not be empty");
		}
		String content = base64OrPem.trim();
		StringBuilder sb = new StringBuilder(content.length());
		for (String line : content.split("\\R")) {
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("-----")) {
				continue;
			}
			sb.append(trimmed);
		}
		try {
			return Base64.getDecoder().decode(sb.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IllegalArgumentException e) {
			throw new JwtException("key content is not valid base64", e);
		}
	}

}
