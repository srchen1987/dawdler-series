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
package club.dawdler.clientplug.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * 分页样式
 */
public class PageStyle {
	public static final String P_MARK = "~p";
	public static final String PAGE_ON_MARK = "(pageOne)";
	public static final String PAGE_COUNT_MARK = "(pageCount)";
	public static final String CONTENT_MARK = "~content_mark";

	private static final Map<String, PageStyleContent> STYLE_CONTENTS = new HashMap<>();
	private static final PageStyle PAGE_STYLE = new PageStyle();

	static {
		PageStyleContent pageStyleContent = PAGE_STYLE.new PageStyleContent();
		pageStyleContent.setFirstPage("<a href=\"" + CONTENT_MARK + "\">首页</a> ");
		pageStyleContent.setUpPage("<a href=\"" + CONTENT_MARK + "\">上一页</a> ");
		pageStyleContent.setPages("<a href=\"" + CONTENT_MARK + "\">" + P_MARK + "</a> ");
		pageStyleContent.setPageOn(P_MARK + " ");
		pageStyleContent.setLastPage("<a href=\"" + CONTENT_MARK + "\">下一页</a> ");
		pageStyleContent.setEndPage("<a href=\"" + CONTENT_MARK + "\">尾页</a>");
		STYLE_CONTENTS.put("default", pageStyleContent);
		export("adminStyle", "<span><a href=\"" + CONTENT_MARK + "\">首页</a></span>",
				"<a class=\"prev\" href=\"" + CONTENT_MARK + "\"></a>",
				"<a href=\"" + CONTENT_MARK + "\">" + P_MARK + "</a>",
				"<strong><font color=\"red\">" + P_MARK + "</font></strong>",
				"<a class=\"nxt\" href=\"" + CONTENT_MARK + "\"></a>",
				"<span><a href=\"" + CONTENT_MARK + "\">尾页</a></span>", null);
		export("bbsStyle", "<a href=\"" + CONTENT_MARK + "\">首页</a>", "<a href=\"" + CONTENT_MARK + "\">上一页</a>",
				"<a href=\"" + CONTENT_MARK + "\">" + P_MARK + "</a>", "<strong>" + P_MARK + "</strong>",
				"<a href=\"" + CONTENT_MARK + "\">下一页</a>", "<a href=\"" + CONTENT_MARK + "\">尾页</a>",
				"<label><input type=\"text\" value=\"" + PAGE_ON_MARK
						+ "\" onkeydown=\"if(event.keyCode==13) {window.location='" + CONTENT_MARK
						+ "'; doane(event);}\" title=\"输入页码，按回车快速跳转\" size=\"2\" class=\"px\" name=\"custompage\"><span title=\"共 "
						+ PAGE_COUNT_MARK + "页\"> / " + PAGE_COUNT_MARK + " 页</span></label>");
	}

	private PageStyle() {

	}

	public static PageStyle getPageStyle() {
		return PAGE_STYLE;
	}

	public static void printPage(int pageOn, int pageCount, int pageNumber, String linkContent, String styleName,
			Writer out) throws IOException {
		PageStyleContent pc = getPageStyle().getPageStyleContent(styleName);
		int start = 1;
		int end;
		if (pageOn < 0) {
			pageOn = 1;
		}
		if (pageNumber > pageCount) {
			pageNumber = pageCount;
		}
		if (pageOn > pageCount) {
			pageOn = pageCount;
		}
		int pageNumber2 = pageNumber / 2;// 取 pageNumber的一半
		boolean sig = pageNumber % 2 == 0;// 取余是否为整数
		if (pageOn > pageNumber2) {// 如果当前页大于了 pageNumber的一半
			start = pageOn - pageNumber2;// 起始值 从 pageOn减去pageNumber2的数开始
			if (sig) {
				start += 1;// 如果没有余数 起始值加一
			}
		}
		if (pageOn + pageNumber2 > pageCount) {// 如果 当前页面加上pageNumber2大于了总页数
			start -= (pageOn + pageNumber2) - pageCount;// 起始值减去 （当前页数加上pageNumber2） 减去 pageCount的值
			if (start < 1) {
				start = 1;// 起始值小于1则等于1
			}
			end = pageCount;// 结束值为 总页数
		} else {
			end = pageOn + pageNumber2;
			if (pageOn <= pageNumber2) {
				end += pageNumber2 - (pageOn - 1);
			}
			if (sig) {
				if (end - start < pageNumber) {
					end += pageNumber - (end - start);// 最主要就是这块比较难理解 因为上面的判断 if(sig)start+=1; 为了 将大数后移一位 在这里找平
				}
				end -= 1;
			}

			if (end > pageCount) {
				end = pageCount;
			}
		}
		if (pageOn > 1) {
			out.write(pc.getFirstPage(linkContent, 1));
			out.write(pc.getUpPage(linkContent, (pageOn - 1)));
		}
		if (pageCount > 1) {
			for (; start <= end; start++) {
				if (start == pageOn) {
					out.write(pc.getPageOn(pageOn));
				} else {
					out.write(pc.getPages(linkContent, start));
				}
			}
		}
		if (pageOn < pageCount) {
			out.write(pc.getLastPage(linkContent, (pageOn + 1)));
			out.write(pc.getEndPage(linkContent, pageCount));
		}
		String stepPage = pc.getStepPage(linkContent, pageOn, pageCount);
		if (stepPage != null) {
			out.write(stepPage);
		}
		out.flush();
	}

	public static void export(String prefix, String first, String up, String pages, String pageOn, String last,
			String end, String stepPage) {
		PageStyleContent pageStyleContent = PAGE_STYLE.new PageStyleContent();
		pageStyleContent.setFirstPage(first);
		pageStyleContent.setUpPage(up);
		pageStyleContent.setPages(pages);
		pageStyleContent.setPageOn(pageOn);
		pageStyleContent.setLastPage(last);
		pageStyleContent.setEndPage(end);
		pageStyleContent.setStepPage(stepPage);
		STYLE_CONTENTS.put(prefix, pageStyleContent);

	}

	public PageStyleContent getPageStyleContentDefault() {
		return STYLE_CONTENTS.get("default");
	}

	public PageStyleContent getPageStyleContent(String styleName) {
		if (styleName == null) {
			return getPageStyleContentDefault();
		}
		PageStyleContent pc = STYLE_CONTENTS.get(styleName);
		if (pc == null) {
			return getPageStyleContentDefault();
		}

		return pc;
	}

	public class PageStyleContent {
		private String pageOn;// 当前页
		private String firstPage;// 首页
		private String upPage;// 上一页
		private String pages;// 循环中的页面
		private String lastPage;// 下一页
		private String endPage;// 结束页
		private String stepPage;// 追加

		public String getPageOn(int pageOn) {
			return this.pageOn.replace(P_MARK, pageOn + "");
		}

		public void setPageOn(String pageOn) {
			this.pageOn = pageOn;
		}

		public String getStepPage(String linkContent, int pageOn, int pageCount) {
			return stepPage != null
					? stepPage.replace(CONTENT_MARK, linkContent).replace(P_MARK, "'+this.value+'")
							.replace(PAGE_ON_MARK, pageOn + "").replace(PAGE_COUNT_MARK, pageCount + "")
					: null;
		}

		public void setStepPage(String stepPage) {
			this.stepPage = stepPage;
		}

		public String getFirstPage(String linkContent, int page) {
			return replaceLinkMark(firstPage, linkContent, page);
		}

		public void setFirstPage(String firstPage) {
			this.firstPage = firstPage;
		}

		public String getUpPage(String linkContent, int page) {
			return replaceLinkMark(upPage, linkContent, page);
		}

		public void setUpPage(String upPage) {
			this.upPage = upPage;
		}

		public String getPages(String linkContent, int page) {
			return replaceLinkMark(pages, linkContent, page);
		}

		public void setPages(String pages) {
			this.pages = pages;
		}

		public String getLastPage(String linkContent, int page) {
			return replaceLinkMark(lastPage, linkContent, page);
		}

		public void setLastPage(String lastPage) {
			this.lastPage = lastPage;
		}

		public String getEndPage(String linkContent, int page) {
			return replaceLinkMark(endPage, linkContent, page);
		}

		public void setEndPage(String endPage) {
			this.endPage = endPage;
		}

		private String replaceLinkMark(String content, String linkContent, int page) {
			return content.replace(CONTENT_MARK, linkContent).replace(P_MARK, "" + page);
		}
	}
}
