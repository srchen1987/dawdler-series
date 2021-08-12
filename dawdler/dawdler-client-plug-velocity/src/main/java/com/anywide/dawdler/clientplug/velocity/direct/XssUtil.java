package com.anywide.dawdler.clientplug.velocity.direct;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * @author jackson.song
 * @version V1.0
 * @Title XssUtil.java
 * @date 2010年4月05日
 * @email suxuan696@gmail.com
 */
public class XssUtil {

	private final static Whitelist user_content_filter = Whitelist.basic();

	static {
		user_content_filter.addTags("embed", "object", "td", "param", "span", "div", "p", "strong", "b", "font", "img",
				"tr", "li", "th", "ul", "br", "h1", "h2", "h3", "h4", "h5", "h6", "ol", "table", "tbody");
		user_content_filter.addAttributes(":all", "style", "type", "class", "id", "name", "color", "src", "width",
				"height");
		user_content_filter.addAttributes("object", "width", "height", "classid", "codebase", "alert");
		user_content_filter.addAttributes("param", "name", "value");
		user_content_filter.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen",
				"allowScriptAccess", "flashvars", "name", "type", "pluginspage");
		user_content_filter.addAttributes("a", "href");
	}

	// 只去除ipt事件或脚本 保留以上定义的
	public static String filterScript(String value) {
		if (value == null) {
			return null;
		}
		return Jsoup.clean(value, user_content_filter);
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