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
package club.dawdler.clientplug.web.jwt.claim;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import club.dawdler.util.JsonProcessUtil;

/**
 * @author jackson.song
 * @version V1.0
 * jwt的claims持有者,采用Map承载,提供标准声明的类型化读写与构建器风格api.
 */
public class JwtClaims {

	private final Map<String, Object> claims;

	public JwtClaims() {
		this.claims = new LinkedHashMap<>();
	}

	public JwtClaims(Map<String, Object> claims) {
		this.claims = claims == null ? new LinkedHashMap<>() : new LinkedHashMap<>(claims);
	}

	public static JwtClaims create() {
		return new JwtClaims();
	}

	public static JwtClaims fromMap(Map<String, Object> claims) {
		return new JwtClaims(claims);
	}

	public Map<String, Object> toMap() {
		return Collections.unmodifiableMap(claims);
	}

	public JwtClaims put(String name, Object value) {
		if (name != null && value != null) {
			claims.put(name, value);
		}
		return this;
	}

	public JwtClaims putAll(Map<String, Object> map) {
		if (map != null) {
			map.forEach(this::put);
		}
		return this;
	}

	public boolean has(String name) {
		return claims.containsKey(name);
	}

	public Object getClaim(String name) {
		return claims.get(name);
	}

	public String getClaimString(String name) {
		Object value = claims.get(name);
		return value == null ? null : value.toString();
	}

	public Long getClaimLong(String name) {
		Object value = claims.get(name);
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		if (value instanceof String) {
			try {
				return Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	public <T> T getClaim(String name, Class<T> type) {
		Object value = claims.get(name);
		if (value == null) {
			return null;
		}
		if (type.isInstance(value)) {
			return type.cast(value);
		}
		return JsonProcessUtil.jsonToBean(JsonProcessUtil.beanToJson(value), type);
	}

	public JwtClaims setIssuer(String issuer) {
		return put(JwtClaimNames.ISSUER, issuer);
	}

	public String getIssuer() {
		return getClaimString(JwtClaimNames.ISSUER);
	}

	public JwtClaims setSubject(String subject) {
		return put(JwtClaimNames.SUBJECT, subject);
	}

	public String getSubject() {
		return getClaimString(JwtClaimNames.SUBJECT);
	}

	public JwtClaims setAudience(String audience) {
		return put(JwtClaimNames.AUDIENCE, audience);
	}

	public JwtClaims setAudience(String... audience) {
		if (audience == null || audience.length == 0) {
			return this;
		}
		List<String> list = new ArrayList<>(audience.length);
		for (String a : audience) {
			if (a != null && !a.isEmpty()) {
				list.add(a);
			}
		}
		if (list.size() == 1) {
			return put(JwtClaimNames.AUDIENCE, list.get(0));
		}
		return put(JwtClaimNames.AUDIENCE, list);
	}

	public List<String> getAudience() {
		Object value = claims.get(JwtClaimNames.AUDIENCE);
		if (value == null) {
			return Collections.emptyList();
		}
		if (value instanceof List) {
			List<String> result = new ArrayList<>();
			for (Object item : (List<?>) value) {
				if (item != null) {
					result.add(item.toString());
				}
			}
			return result;
		}
		return Collections.singletonList(value.toString());
	}

	public JwtClaims setExpiration(long epochSecond) {
		return put(JwtClaimNames.EXPIRATION, epochSecond);
	}

	public JwtClaims setExpiration(Date expiration) {
		return expiration == null ? this : setExpiration(expiration.getTime() / 1000);
	}

	public JwtClaims setExpiration(Instant expiration) {
		return expiration == null ? this : setExpiration(expiration.getEpochSecond());
	}

	public Long getExpiration() {
		return getClaimLong(JwtClaimNames.EXPIRATION);
	}

	public Date getExpirationDate() {
		Long epochSecond = getExpiration();
		return epochSecond == null ? null : new Date(epochSecond * 1000L);
	}

	public JwtClaims setNotBefore(long epochSecond) {
		return put(JwtClaimNames.NOT_BEFORE, epochSecond);
	}

	public JwtClaims setNotBefore(Date notBefore) {
		return notBefore == null ? this : setNotBefore(notBefore.getTime() / 1000);
	}

	public JwtClaims setNotBefore(Instant notBefore) {
		return notBefore == null ? this : setNotBefore(notBefore.getEpochSecond());
	}

	public Long getNotBefore() {
		return getClaimLong(JwtClaimNames.NOT_BEFORE);
	}

	public Date getNotBeforeDate() {
		Long epochSecond = getNotBefore();
		return epochSecond == null ? null : new Date(epochSecond * 1000L);
	}

	public JwtClaims setIssuedAt(long epochSecond) {
		return put(JwtClaimNames.ISSUED_AT, epochSecond);
	}

	public JwtClaims setIssuedAt(Date issuedAt) {
		return issuedAt == null ? this : setIssuedAt(issuedAt.getTime() / 1000);
	}

	public JwtClaims setIssuedAt(Instant issuedAt) {
		return issuedAt == null ? this : setIssuedAt(issuedAt.getEpochSecond());
	}

	public Long getIssuedAt() {
		return getClaimLong(JwtClaimNames.ISSUED_AT);
	}

	public Date getIssuedDate() {
		Long epochSecond = getIssuedAt();
		return epochSecond == null ? null : new Date(epochSecond * 1000L);
	}

	public JwtClaims setId(String id) {
		return put(JwtClaimNames.JWT_ID, id);
	}

	public String getId() {
		return getClaimString(JwtClaimNames.JWT_ID);
	}

	@Override
	public String toString() {
		return claims.toString();
	}

}
