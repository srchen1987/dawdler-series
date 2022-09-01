package com.anywide.dawdler.clientplug.velocity.direct;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * @author jackson.song
 * @version V1.0
 * @Title XssUtil.java
 * @date 2010年4月5日
 * @email suxuan696@gmail.com
 */
public class XssUtil {

	private final static Safelist USER_CONTENT_FILTER = Safelist.basic();

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

	// 只去除ipt事件或脚本 保留以上定义的
	public static String filterScript(String value) {
		if (value == null) {
			return null;
		}
		return Jsoup.clean(value, USER_CONTENT_FILTER);
	}

	// 替换所有脚本 转换成&lt; &gt; 等等
	public static String filter(String value) {
		if (value == null) {
			return null;
		}
		char[] content = value.toCharArray();
		StringBuffer result = new StringBuffer(content.length + 30);
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