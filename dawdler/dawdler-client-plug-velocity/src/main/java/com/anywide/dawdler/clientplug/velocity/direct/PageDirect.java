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
package com.anywide.dawdler.clientplug.velocity.direct;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import com.anywide.dawdler.clientplug.velocity.PageStyle;
import com.anywide.dawdler.serverplug.load.bean.Page;
import com.anywide.dawdler.util.ToolEL;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ControlDirect.java
 * @Description 自定义控件的指令 （注释后补的）
 * @date 2007年04月18日
 * @email suxuan696@gmail.com
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
	public boolean render(InternalContextAdapter arg0, Writer arg1, Node arg2)
			throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
		String stylename = null;
		String linkcontent = arg2.jjtGetChild(0).value(arg0) + "";
		if (arg2.jjtGetNumChildren() > 1) {
			stylename = arg2.jjtGetChild(1).value(arg0) + "";
		}
		Object pageobj = arg0.get("page");
		Page page = null;
		if (pageobj == null) {
			Object action = arg0.get("action");
			page = (Page) ToolEL.getBeanValue(action, "page");
		} else {
			page = (Page) pageobj;
		}
		if (page == null)
			throw new ParseErrorException("not set page in action !");
		PageStyle.printPage(page.getPageon(), page.getPagecount(), page.getPageNumber(), linkcontent, stylename, arg1);
		return true;
	}

}
