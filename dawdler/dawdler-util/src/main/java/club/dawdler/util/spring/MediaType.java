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

import java.util.Map;

/**
 * Adapted from Spring Framework's org.springframework.http.MediaType.
 * Adds common media type constants used in content negotiation.
 */
public class MediaType extends MimeType {

	private static final long serialVersionUID = 1L;

	public static final String ALL_VALUE = "*/*";

	public static final String APPLICATION_JSON_VALUE = "application/json";

	public static final String APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8";

	public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

	public static final String APPLICATION_XML_VALUE = "application/xml";

	public static final String TEXT_HTML_VALUE = "text/html";

	public static final String TEXT_HTML_UTF8_VALUE = "text/html;charset=UTF-8";

	public static final String TEXT_PLAIN_VALUE = "text/plain";

	public static final String TEXT_XML_VALUE = "text/xml";

	public static final String TEXT_EVENT_STREAM_VALUE = "text/event-stream";

	public static final MediaType ALL = new MediaType("*", "*");

	public static final MediaType APPLICATION_JSON = new MediaType("application", "json");

	public static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");

	public static final MediaType APPLICATION_XML = new MediaType("application", "xml");

	public static final MediaType TEXT_HTML = new MediaType("text", "html");

	public static final MediaType TEXT_PLAIN = new MediaType("text", "plain");

	public static final MediaType TEXT_XML = new MediaType("text", "xml");

	public static final MediaType TEXT_EVENT_STREAM = new MediaType("text", "event-stream");

	public MediaType(String type, String subtype) {
		super(type, subtype);
	}

	public MediaType(String type, String subtype, Map<String, String> parameters) {
		super(type, subtype, parameters);
	}

	public static MediaType valueOf(String value) {
		MimeType mime = MimeType.valueOf(value);
		return new MediaType(mime.getType(), mime.getSubtype(), mime.getParameters());
	}
}
