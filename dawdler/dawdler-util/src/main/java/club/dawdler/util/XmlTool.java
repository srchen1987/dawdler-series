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
package club.dawdler.util;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author jackson.song
 * @version V1.0
 * xml操作类
 */
public final class XmlTool {
	private static final ErrorHandler ERRORHANDLER_INSTANCE;
	static {
		ERRORHANDLER_INSTANCE = new ErrorHandler() {
			@Override
			public void warning(SAXParseException exception) throws SAXException {
				throw exception;
			}

			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				throw exception;
			}

			@Override
			public void error(SAXParseException exception) throws SAXException {
				throw exception;
			}
		};
	}

	public static ErrorHandler getErrorHandler() {
		return ERRORHANDLER_INSTANCE;
	}

	public static List<Node> getNodes(NodeList nodeList) {
		int length = nodeList.getLength();
		List<Node> list = new ArrayList<>();
		if (nodeList != null && length > 0) {
			for (int i = 0; i < length; i++) {
				Node node = nodeList.item(i);
				list.add(node);
			}
		}
		return list;
	}
 

}
