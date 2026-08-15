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
package club.dawdler.clientplug.velocity.direct;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * @author jackson.song
 * @version V1.0
 */
public class XssUtil {
	private XssUtil() {
	}

	private static final Safelist USER_CONTENT_FILTER = Safelist.basic();

	static {
		USER_CONTENT_FILTER.addTags("embed", "object", "td", "param", "span", "div", "p", "strong", "b", "font", "img",
				"tr", "li", "th", "ul", "br", "h1", "h2", "h3", "h4", "h5", "h6", "ol", "table", "tbody");
		USER_CONTENT_FILTER.addAttributes(":all", "style", "type", "class", "id", "name", "color", "src", "width",
				"height");
		USER_CONTENT_FILTER.addAttributes("object", "width", "height", "classid", "codebase", "alert");
		USER_CONTENT_FILTER.addAttributes("param", "name", "value");
		USER_CONTENT_FILTER.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen",
				"allowScriptAccess", "flashvars", "name", "type", "pluginspage");
		USER_CONTENT_FILTER.addAttributes("a", "href");
	}

	public static String filterScript(String value) {
		if (value == null) {
			return null;
		}
		return Jsoup.clean(value, USER_CONTENT_FILTER);
	}

	public static String filter(String value) {
		if (value == null) {
			return null;
		}
		char[] content = value.toCharArray();
		StringBuilder result = new StringBuilder(content.length + 30);
		for (int i = 0; i < content.length; i++) {
			switch (content[i]) {
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '"':
				result.append("&quot;");
				break;
			case '\'':
				result.append("&#39;");
				break;
			default:
				result.append(content[i]);
			}
		}
		return result.toString();
	}
}
