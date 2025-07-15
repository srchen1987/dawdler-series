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

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import club.dawdler.clientplug.velocity.PageStyle;
import club.dawdler.serverplug.load.bean.Page;

/**
 * @author jackson.song
 * @version V1.0
 * 自定义控件的指令
 */
public class PageDirect extends Directive {
	@Override
	public String getName() {
		return "pages";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render(InternalContextAdapter context, Writer writer, Node node)
			throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
		String styleName = null;
		String linkContent = node.jjtGetChild(0).value(context) + "";
		if (node.jjtGetNumChildren() > 1) {
			styleName = node.jjtGetChild(1).value(context) + "";
		}
		Object pageObj = context.get("page");
		Page page = null;
		if (pageObj != null) {
			page = (Page) pageObj;
		}
		if (page == null) {
			throw new ParseErrorException("not set page in action !");
		}
		PageStyle.printPage(page.getPageOn(), page.getPageCount(), page.getPageNumber(), linkContent, styleName, writer);
		return true;
	}

}
