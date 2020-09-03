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
package com.anywide.dawdler.clientplug.velocity;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @Title: PageStyle.java
 * @Description: 分页样式 （注释后补的）
 * @author: jackson.song
 * @date: 2006年08月10日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class PageStyle {
	public static final String PMARK="~p";
	public static final String PAGEONMARK="(pageone)";
	public static final String PAGECOUNTMARK="(pagecount)";
	public static final String CONTENTMARK="~content_mark";
	
	private static Map<String, PageStyleContent> stylecontents = new HashMap();
	private static PageStyle pageStyle=new PageStyle(); 
	public static PageStyle getPageStyle(){
		return pageStyle;
	}
	private PageStyle(){
		
	}
	public static void printPage(int pageon,int pagecount,int pagenumber,String linkcontent,String stylename,Writer out) throws IOException{
		PageStyleContent pc =getPageStyle().getPageStyleContent(stylename);
		int start=1;
		int end;
		if(pageon<0)pageon=1;
		if(pagenumber>pagecount)pagenumber=pagecount;
		if(pageon>pagecount)pageon=pagecount;
		int pagenumber2 = pagenumber/2;//取 pagenumber的一半
		boolean sig =  pagenumber%2==0;//取余是否为整数
		if(pageon>pagenumber2){//如果当前页大于了 pagenumber的一半
			start = pageon-pagenumber2;//起始值 从 pageon减去pagenumber2的数开始
			if(sig)start+=1;//如果没有余数 起始值加一
		}
		if(pageon+pagenumber2>pagecount){//如果 当前页面加上pagenumber2大于了总页数
			start-=(pageon+pagenumber2)-pagecount;//起始值减去 （当前页数加上pagenumber2） 减去 pagecount的值
			if(start<1)start=1;//起始值小于1则等于1
			end = pagecount;//结束值为 总页数
		}else{
			end = pageon+pagenumber2;
			if(pageon<=pagenumber2){
				end+=pagenumber2-(pageon-1);
			}
			if(sig){
				if(end-start<pagenumber){
					end+=pagenumber-(end-start);//最主要就是这块比较难理解 因为上面的判断  if(sig)start+=1; 为了 将大数后移一位  在这里找平
				}
				end-=1;
			}
		
			if(end>pagecount)end = pagecount;
		}
		if(pageon>1){
			out.write(pc.getFirstpage(linkcontent, 1));
			out.write(pc.getUppage(linkcontent,(pageon-1)));
		}
		if(pagecount>1)
		for(;start<=end;start++){
			if(start==pageon)
			out.write(pc.getPageon(pageon));
			else
				out.write(pc.getPages(linkcontent, start));
		}
		if(pageon<pagecount){
			out.write(pc.getLastpage(linkcontent,(pageon+1)));
			out.write(pc.getEndpage(linkcontent,pagecount));
		}
		String steppage = pc.getSteppage(linkcontent,pageon,pagecount);
		if(steppage!=null){
			out.write(steppage);
		}
	}
	static{
		PageStyleContent pcont = pageStyle.new PageStyleContent();
		pcont.setFirstpage("<a href=\""+CONTENTMARK+"\">首页</a> ");
		pcont.setUppage("<a href=\""+CONTENTMARK+"\">上一页</a> ");
		pcont.setPages("<a href=\""+CONTENTMARK+"\">"+PMARK+"</a> ");
		pcont.setPageon(PMARK+" ");
		pcont.setLastpage("<a href=\""+CONTENTMARK+"\">下一页</a> ");
		pcont.setEndpage("<a href=\""+CONTENTMARK+"\">尾页</a>");
		stylecontents.put("default",pcont);
		export("adminstyle","<span><a href=\""+CONTENTMARK+"\">首页</a></span>","<a class=\"prev\" href=\""+CONTENTMARK+"\"></a>","<a href=\""+CONTENTMARK+"\">"+PMARK+"</a>","<strong><font color=\"red\">"+PMARK+"</font></strong>","<a class=\"nxt\" href=\""+CONTENTMARK+"\"></a>","<span><a href=\""+CONTENTMARK+"\">尾页</a></span>",null);
		export("bbsstyle","<a href=\""+CONTENTMARK+"\">首页</a>","<a href=\""+CONTENTMARK+"\">上一页</a>","<a href=\""+CONTENTMARK+"\">"+PMARK+"</a>","<strong>"+PMARK+"</strong>","<a href=\""+CONTENTMARK+"\">下一页</a>","<a href=\""+CONTENTMARK+"\">尾页</a>","<label><input type=\"text\" value=\""+PAGEONMARK+"\" onkeydown=\"if(event.keyCode==13) {window.location='"+CONTENTMARK+"'; doane(event);}\" title=\"输入页码，按回车快速跳转\" size=\"2\" class=\"px\" name=\"custompage\"><span title=\"共 "+PAGECOUNTMARK+"页\"> / "+PAGECOUNTMARK+" 页</span></label>");
	}
	public static void export(String prefix,String first,String up,String pages,String pageon,String last,String end,String steppage){
		PageStyleContent pcont = pageStyle.new PageStyleContent();
		pcont.setFirstpage(first);
		pcont.setUppage(up);
		pcont.setPages(pages);
		pcont.setPageon(pageon);
		pcont.setLastpage(last);
		pcont.setEndpage(end);
		pcont.setSteppage(steppage);
		stylecontents.put(prefix,pcont);
		
	}
	public PageStyleContent getPageStyleContentDefault(){
		return stylecontents.get("default");
	}
	public PageStyleContent getPageStyleContent(String stylename){
		if(stylename==null)return getPageStyleContentDefault();
		PageStyleContent pc = stylecontents.get(stylename);
		if(pc==null)return getPageStyleContentDefault();
		return pc;
	}
	private String replaceLinkMark(String content,String linkcontent,int page){
		 return content.replace(CONTENTMARK,linkcontent).replace(PMARK,""+page);
	}
	public class PageStyleContent{
		private String pageon;//当前页
		private String firstpage;//首页
		private String uppage;//上一页
		private String pages;//循环中的页面
		private String lastpage;//下一页
		private String endpage;//结束页
		private String steppage;//追加
		public String getPageon(int pageon) {
			return this.pageon.replace(PMARK,pageon+"");
		}
		public void setPageon(String pageon) {
			this.pageon = pageon;
		}
		public String getSteppage(String linkcontent,int pageon,int pagecount){
			return steppage!=null?steppage.replace(CONTENTMARK,linkcontent).replace(PMARK,"'+this.value+'").replace(PAGEONMARK,pageon+"").replace(PAGECOUNTMARK,pagecount+""):null;
		}
		public void setSteppage(String steppage) {
			this.steppage = steppage;
		}
		public String getFirstpage(String linkcontent,int page) {
			return replaceLinkMark(firstpage, linkcontent, page);
		}
		public void setFirstpage(String firstpage) {
			this.firstpage = firstpage;
		}
		public String getUppage(String linkcontent,int page) {
			return replaceLinkMark(uppage, linkcontent, page);
		}
		public void setUppage(String uppage) {
			this.uppage = uppage;
		}
		public String getPages(String linkcontent,int page) {
			return replaceLinkMark(pages, linkcontent, page);
		}
		public void setPages(String pages) {
			this.pages = pages;
		}
		public String getLastpage(String linkcontent,int page) {
			return replaceLinkMark(lastpage, linkcontent, page);
		}
		public void setLastpage(String lastpage) {
			this.lastpage = lastpage;
		}
		public String getEndpage(String linkcontent,int page) {
			return replaceLinkMark(endpage, linkcontent, page);
		}
		public void setEndpage(String endpage) {
			this.endpage = endpage;
		}
	}
}
