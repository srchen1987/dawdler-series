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
package club.dawdler.clientplug.web.jwt.operator;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import club.dawdler.clientplug.web.jwt.algorithm.Algorithm;
import club.dawdler.clientplug.web.jwt.claim.JwtClaimNames;
import club.dawdler.clientplug.web.jwt.claim.JwtClaims;
import club.dawdler.clientplug.web.jwt.exception.JwtException;
import club.dawdler.clientplug.web.jwt.exception.JwtExpiredException;
import club.dawdler.clientplug.web.jwt.exception.JwtNotYetValidException;
import club.dawdler.clientplug.web.jwt.exception.JwtParseException;
import club.dawdler.clientplug.web.jwt.exception.JwtSignatureException;
import club.dawdler.util.JsonProcessUtil;

/**
 * @author jackson.song
 * @version V1.0
 * jwt核心操作器,负责token的签发,解析与校验,实现RFC7519.
 */
public class JwtOperator {

	private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

	private final Algorithm algorithm;
	private final String issuer;
	private final Set<String> audiences;
	private final long leewaySeconds;
	private final long defaultExpireSeconds;

	public JwtOperator(Algorithm algorithm, String issuer, Set<String> audiences, long leewaySeconds,
			long defaultExpireSeconds) {
		if (algorithm == null) {
			throw new JwtException("algorithm can not be null");
		}
		this.algorithm = algorithm;
		this.issuer = issuer;
		this.audiences = audiences == null ? Collections.emptySet() : audiences;
		this.leewaySeconds = leewaySeconds;
		this.defaultExpireSeconds = defaultExpireSeconds;
	}

	public String sign(JwtClaims claims) {
		if (claims == null) {
			throw new JwtException("claims can not be null");
		}
		JwtClaims target = JwtClaims.fromMap(claims.toMap());
		long now = Instant.now().getEpochSecond();
		if (!target.has(JwtClaimNames.ISSUED_AT)) {
			target.setIssuedAt(now);
		}
		if (!target.has(JwtClaimNames.EXPIRATION) && defaultExpireSeconds > 0) {
			target.setExpiration(now + defaultExpireSeconds);
		}
		Map<String, Object> header = new LinkedHashMap<>();
		header.put(JwtClaimNames.ALGORITHM, algorithm.getName());
		header.put(JwtClaimNames.TYP, "JWT");

		String headerSegment = encodeJson(header);
		String payloadSegment = encodeJson(target.toMap());
		String signingInput = headerSegment + "." + payloadSegment;
		byte[] signature = algorithm.sign(signingInput.getBytes(StandardCharsets.UTF_8));
		return signingInput + "." + URL_ENCODER.encodeToString(signature);
	}

	public JwtClaims parse(String token) {
		if (token == null || token.isEmpty()) {
			throw new JwtParseException("token can not be empty");
		}
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new JwtParseException("token must have three segments separated by '.'");
		}
		Map<String, Object> header = decodeJson(parts[0]);
		Object algValue = header.get(JwtClaimNames.ALGORITHM);
		if (!(algValue instanceof String)) {
			throw new JwtSignatureException("token header missing alg");
		}
		String alg = (String) algValue;
		if ("none".equalsIgnoreCase(alg)) {
			throw new JwtSignatureException("algorithm 'none' is not allowed");
		}
		if (!alg.equals(algorithm.getName())) {
			throw new JwtSignatureException("algorithm mismatch, expected " + algorithm.getName() + " but got " + alg);
		}
		byte[] signingInput = (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8);
		byte[] signature;
		try {
			signature = URL_DECODER.decode(parts[2]);
		} catch (IllegalArgumentException e) {
			throw new JwtSignatureException("signature segment is not valid base64url", e);
		}
		if (!algorithm.verify(signingInput, signature)) {
			throw new JwtSignatureException("invalid jwt signature");
		}
		Map<String, Object> claimsMap = decodeJson(parts[1]);
		JwtClaims claims = JwtClaims.fromMap(claimsMap);
		validateClaims(claims);
		return claims;
	}

	private void validateClaims(JwtClaims claims) {
		long now = Instant.now().getEpochSecond();
		Long exp = claims.getExpiration();
		if (exp != null && now >= exp + leewaySeconds) {
			throw new JwtExpiredException("jwt expired at " + exp);
		}
		Long nbf = claims.getNotBefore();
		if (nbf != null && now + leewaySeconds < nbf) {
			throw new JwtNotYetValidException("jwt not valid before " + nbf);
		}
		if (issuer != null && !issuer.isEmpty()) {
			if (!issuer.equals(claims.getIssuer())) {
				throw new JwtParseException("invalid issuer, expected " + issuer);
			}
		}
		if (!audiences.isEmpty()) {
			boolean matched = false;
			for (String aud : claims.getAudience()) {
				if (audiences.contains(aud)) {
					matched = true;
					break;
				}
			}
			if (!matched) {
				throw new JwtParseException("invalid audience");
			}
		}
	}

	private static String encodeJson(Map<String, Object> map) {
		try {
			byte[] json = JsonProcessUtil.beanToJsonByte(map);
			return URL_ENCODER.encodeToString(json);
		} catch (Exception e) {
			throw new JwtException("encode json error", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> decodeJson(String segment) {
		byte[] json;
		try {
			json = URL_DECODER.decode(segment);
		} catch (IllegalArgumentException e) {
			throw new JwtParseException("segment is not valid base64url", e);
		}
		try {
			Object result = JsonProcessUtil.jsonToBean(json, Map.class);
			if (result == null) {
				return Collections.emptyMap();
			}
			return (Map<String, Object>) result;
		} catch (Exception e) {
			throw new JwtParseException("decode json error", e);
		}
	}

}
