/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.dawdler.util.spring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapted from Spring Framework's org.springframework.util.MimeType.
 * Represents a MIME Type, supports wildcards and quality factors for content negotiation.
 */
public class MimeType implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String WILDCARD_TYPE = "*";

	private static final String PARAM_QUALITY = "q";

	private final String type;

	private final String subtype;

	private final Map<String, String> parameters;

	public MimeType(String type) {
		this(type, WILDCARD_TYPE);
	}

	public MimeType(String type, String subtype) {
		this(type, subtype, Collections.<String, String> emptyMap());
	}

	public MimeType(String type, String subtype, Map<String, String> parameters) {
		if (type == null || type.isEmpty()) {
			throw new IllegalArgumentException("Type must not be empty");
		}
		if (subtype == null || subtype.isEmpty()) {
			throw new IllegalArgumentException("Subtype must not be empty");
		}
		checkToken(type);
		checkToken(subtype);
		this.type = type.toLowerCase();
		this.subtype = subtype.toLowerCase();
		if (parameters.isEmpty()) {
			this.parameters = Collections.emptyMap();
		} else {
			Map<String, String> map = new LinkedHashMap<>(parameters.size());
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				checkParameters(key, value);
				map.put(key, value);
			}
			this.parameters = Collections.unmodifiableMap(map);
		}
	}

	private void checkToken(String token) {
		for (int i = 0; i < token.length(); i++) {
			char ch = token.charAt(i);
			if (!TOKEN.get(ch)) {
				throw new IllegalArgumentException("Invalid token character '" + ch + "' in token \"" + token + "\"");
			}
		}
	}

	private void checkParameters(String attribute, String value) {
		for (int i = 0; i < attribute.length(); i++) {
			char ch = attribute.charAt(i);
			if (!TOKEN.get(ch)) {
				throw new IllegalArgumentException(
						"Invalid attribute character '" + ch + "' in attribute \"" + attribute + "\"");
			}
		}
		if (PARAM_QUALITY.equals(attribute)) {
			try {
				double d = Double.parseDouble(value);
				if (d < 0D || d > 1D) {
					throw new IllegalArgumentException("Invalid quality value \"" + value + "\": should be between 0.0 and 1.0");
				}
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Invalid quality value \"" + value + "\": should be a double");
			}
		}
	}

	public boolean isWildcardType() {
		return WILDCARD_TYPE.equals(type);
	}

	public boolean isWildcardSubtype() {
		return WILDCARD_TYPE.equals(subtype) || subtype.startsWith("*+");
	}

	public String getType() {
		return type;
	}

	public String getSubtype() {
		return subtype;
	}

	public String getParameter(String name) {
		return parameters.get(name);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public double getQualityValue() {
		String quality = parameters.get(PARAM_QUALITY);
		if (quality == null) {
			return 1D;
		}
		try {
			return Double.parseDouble(quality);
		} catch (NumberFormatException ex) {
			return 1D;
		}
	}

	public boolean isCompatibleWith(MimeType other) {
		if (other == null) {
			return false;
		}
		if (isWildcardType() || other.isWildcardType()) {
			return true;
		} else if (getType().equals(other.getType())) {
			if (getSubtype().equals(other.getSubtype())) {
				return true;
			}
			if (isWildcardSubtype() || other.isWildcardSubtype()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MimeType)) {
			return false;
		}
		MimeType otherType = (MimeType) other;
		return type.equalsIgnoreCase(otherType.type) && subtype.equalsIgnoreCase(otherType.subtype)
				&& parametersAreEqual(otherType);
	}

	private boolean parametersAreEqual(MimeType other) {
		if (parameters.size() != other.parameters.size()) {
			return false;
		}
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			String key = entry.getKey();
			if (!other.parameters.containsKey(key)) {
				return false;
			}
			if (PARAM_QUALITY.equals(key)) {
				continue;
			}
			if (!entry.getValue().equals(other.parameters.get(key))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = type.hashCode();
		result = 31 * result + subtype.hashCode();
		result = 31 * result + parameters.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(type);
		builder.append('/');
		builder.append(subtype);
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (!PARAM_QUALITY.equals(entry.getKey())) {
				builder.append(';');
				builder.append(entry.getKey());
				builder.append('=');
				builder.append(entry.getValue());
			}
		}
		return builder.toString();
	}

	public static MimeType valueOf(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Value must not be null");
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("Value must not be empty");
		}
		String[] parts = tokenize(trimmed, ';');
		if (parts.length == 0) {
			throw new IllegalArgumentException("Invalid mime type: " + value);
		}
		String fullType = parts[0].trim();
		if (fullType.isEmpty()) {
			throw new IllegalArgumentException("Invalid mime type: " + value);
		}
		if (fullType.equals("*")) {
			fullType = "*/*";
		}
		int slashIndex = fullType.indexOf('/');
		if (slashIndex < 0) {
			throw new IllegalArgumentException("Invalid mime type: " + value);
		}
		String type = fullType.substring(0, slashIndex).trim();
		String subtype = fullType.substring(slashIndex + 1).trim();
		if (type.isEmpty() || subtype.isEmpty()) {
			throw new IllegalArgumentException("Invalid mime type: " + value);
		}
		Map<String, String> params = Collections.emptyMap();
		if (parts.length > 1) {
			params = new LinkedHashMap<>(parts.length - 1);
			for (int i = 1; i < parts.length; i++) {
				String param = parts[i].trim();
				if (param.isEmpty()) {
					continue;
				}
				int eqIndex = param.indexOf('=');
				if (eqIndex > 0) {
					String key = param.substring(0, eqIndex).trim();
					String val = param.substring(eqIndex + 1).trim();
					params.put(key, val);
				}
			}
		}
		return new MimeType(type, subtype, params);
	}

	public static List<MimeType> parseMediaTypes(String mediaTypes) {
		if (mediaTypes == null) {
			return Collections.emptyList();
		}
		String trimmed = mediaTypes.trim();
		if (trimmed.isEmpty()) {
			return Collections.emptyList();
		}
		String[] tokens = tokenize(trimmed, ',');
		List<MimeType> result = new ArrayList<>(tokens.length);
		for (String token : tokens) {
			String t = token.trim();
			if (!t.isEmpty()) {
				result.add(MimeType.valueOf(t));
			}
		}
		return result;
	}

	private static String[] tokenize(String str, char delimiter) {
		List<String> tokens = new ArrayList<>(4);
		StringBuilder sb = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == '"') {
				inQuotes = !inQuotes;
				sb.append(ch);
			} else if (ch == delimiter && !inQuotes) {
				tokens.add(sb.toString());
				sb.setLength(0);
			} else {
				sb.append(ch);
			}
		}
		tokens.add(sb.toString());
		return tokens.toArray(new String[0]);
	}

	private static final java.util.BitSet TOKEN;

	static {
		java.util.BitSet token = new java.util.BitSet(128);
		for (int i = 32; i < 127; i++) {
			token.set(i);
		}
		token.set('(');
		token.set(')');
		token.set('<');
		token.set('>');
		token.set('@');
		token.set(',');
		token.set(';');
		token.set(':');
		token.set('\\');
		token.set('"');
		token.set('/');
		token.set('[');
		token.set(']');
		token.set('?');
		token.set('=');
		token.set('{');
		token.set('}');
		token.set(' ');
		token.set('\t');
		token.flip(32, 127);
		TOKEN = token;
	}
}
