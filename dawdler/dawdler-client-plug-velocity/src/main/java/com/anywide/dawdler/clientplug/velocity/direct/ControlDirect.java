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
import java.lang.reflect.Field;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import com.anywide.dawdler.clientplug.dynamicform.control.ControlFactory;
import com.anywide.dawdler.clientplug.velocity.ControlTag;

/**
 * @author jackson.song
 * @version V1.0
 * 自定义控件的指令
 */
public class ControlDirect extends Directive {

	@Override
	public String getName() {
		return "control";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render(InternalContextAdapter arg0, Writer arg1, Node arg2)
			throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
		Node node = arg2.jjtGetChild(0);
		Object object = node.value(arg0);
		if (object instanceof ControlTag) {
			ControlTag controlTag = (ControlTag) object;
			arg1.write(ControlFactory.getControl(controlTag).showView());
			return true;
		}
		int count = arg2.jjtGetNumChildren();
		ControlTag ct = new ControlTag();
		Field[] fs = ControlTag.class.getDeclaredFields();
		for (int i = 0; i < count; i++) {
			Node nodeTemp = arg2.jjtGetChild(i);
			Object obj = nodeTemp.value(arg0);
			if (!obj.toString().equals("")) {
				try {
					fs[i].setAccessible(true);
					fs[i].set(ct, obj.toString());
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
		arg1.write(ControlFactory.getControl(ct).showView());
		return true;
	}

}
