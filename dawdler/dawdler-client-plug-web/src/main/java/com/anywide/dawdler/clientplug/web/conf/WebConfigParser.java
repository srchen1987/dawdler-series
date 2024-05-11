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
package com.anywide.dawdler.clientplug.web.conf;

import static com.anywide.dawdler.util.XmlTool.getNodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebConfigParser.java
 * @Description web配置解析器
 * @date 2024年2月9日
 * @email suxuan696@gmail.com
 */
public class WebConfigParser {
	private static final Logger logger = LoggerFactory.getLogger(WebConfigParser.class);
	private static XmlObject xmlObject;
	private static WebConfig webConfig;
	static {
		String fileName;
		String activeProfile = System.getProperty("dawdler.profiles.active");
		String prefix = "web-conf";
		String subfix = ".xml";
		InputStream xsdInput = WebConfigParser.class.getResourceAsStream("/web-conf.xsd");
		fileName = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + subfix;
		InputStream input = DawdlerTool.getResourceFromClassPath(prefix, subfix);
		if (input == null) {
			logger.error("not found " + fileName);
		} else {
			try {
				xmlObject = new XmlObject(input, xsdInput);
				xmlObject.setPrefix("ns");
				webConfig = new WebConfig();
				Document root = xmlObject.getDocument();
				List<Node> childNodes = getNodes(root.getDocumentElement().getChildNodes());
				for (Node childNode : childNodes) {
					String childNodeName = childNode.getNodeName();
					if (childNodeName.equals("mybatis")) {
						loadMappers(childNode);
					} else if (childNodeName.equals("scanner")) {
						List<Node> scannerChildNodes = getNodes(childNode.getChildNodes());
						for (Node scannerChildNode : scannerChildNodes) {
							String scannerChildNodeName = scannerChildNode.getNodeName();
							if (scannerChildNodeName.equals("package-paths")) {
								loadPackagesInClasses(scannerChildNode);
							}
						}
					} else if (childNodeName.equals("datasource-expressions")) {
						loadDataSourceExpressions(childNode);
					} else if (childNodeName.equals("decisions")) {
						loadDecisions(childNode);
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static WebConfig getWebConfig() {
		return webConfig;
	}

	public static void loadMappers(Node node) {
		List<Node> mappers = getNodes(node.getLastChild().getChildNodes());
		if (!mappers.isEmpty()) {
			Set<String> mapperSet = new HashSet<>();
			for (Node mapper : mappers) {
				mapperSet.add(mapper.getTextContent().trim());
			}
			webConfig.setMappers(mapperSet);
		}
	}

	public static void loadPackagesInClasses(Node node) {
		List<Node> packagePaths = getNodes(node.getChildNodes());
		if (!packagePaths.isEmpty()) {
			Set<String> packagePathsSet = new HashSet<>();
			for (Node packagePath : packagePaths) {
				packagePathsSet.add(packagePath.getTextContent().trim());
			}
			webConfig.setPackagePaths(packagePathsSet);
		}
	}

	public static void loadDataSourceExpressions(Node node) {
		List<Node> dataSourceExpressions = getNodes(node.getChildNodes());
		if (!dataSourceExpressions.isEmpty()) {
			List<Map<String, String>> dataSourceExpressionList = new ArrayList<>();
			for (Node datasourceExpressionNode : dataSourceExpressions) {
				Map<String, String> dataSourceExpression = new HashMap<>();
				NamedNodeMap namedNodeMap = datasourceExpressionNode.getAttributes();
				dataSourceExpression.put("id", namedNodeMap.getNamedItem("id").getNodeValue());
				dataSourceExpression.put("latentExpression",
						namedNodeMap.getNamedItem("latent-expression").getNodeValue());
				dataSourceExpressionList.add(dataSourceExpression);
			}
			webConfig.setDataSourceExpressions(dataSourceExpressionList);
		}
	}

	public static void loadDecisions(Node node) {
		List<Node> decisions = getNodes(node.getChildNodes());
		if (!decisions.isEmpty()) {
			List<Map<String, String>> decisionList = new ArrayList<>();
			for (Node decisionNode : decisions) {
				Map<String, String> decision = new HashMap<>();
				NamedNodeMap namedNodeMap = decisionNode.getAttributes();
				decision.put("latentExpressionId", namedNodeMap.getNamedItem("latent-expression-id").getNodeValue());
				decision.put("mapping", namedNodeMap.getNamedItem("mapping").getNodeValue());
				decisionList.add(decision);
			}
			webConfig.setDecisions(decisionList);
		}
	}

}
