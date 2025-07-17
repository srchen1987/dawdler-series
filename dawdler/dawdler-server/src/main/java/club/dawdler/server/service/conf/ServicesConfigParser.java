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
package club.dawdler.server.service.conf;

import static club.dawdler.util.XmlTool.getNodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import club.dawdler.util.DawdlerTool;
import club.dawdler.util.XmlObject;

/**
 * @author jackson.song
 * @version V1.0
 * 服务配置解析类 (抛弃老版本的xstream实现. 使用dom4j实现,最后抛弃dom4j实现)
 */
public class ServicesConfigParser {
	private ServicesConfig servicesConfig = new ServicesConfig();

	public ServicesConfig getServicesConfig() {
		return servicesConfig;
	}

	public void loadRemoteLoad(Node node) {
		Node packageNode = node.getAttributes().getNamedItem("package");
		if (packageNode != null) {
			servicesConfig.setRemoteLoad(packageNode.getNodeValue());
		}
	}

	public void loadMappers(Node node) {
		List<Node> mappers = getNodes(node.getLastChild().getChildNodes());
		if (!mappers.isEmpty()) {
			Set<String> mapperSet = new HashSet<>();
			for (Node mapper : mappers) {
				mapperSet.add(mapper.getTextContent().trim());
			}
			servicesConfig.setMappers(mapperSet);
		}
	}

	public void loadPreLoads(Node node) {
		List<Node> preLoads = getNodes(node.getChildNodes());
		if (!preLoads.isEmpty()) {
			Set<String> preLoadSet = new HashSet<>();
			for (Node preLoad : preLoads) {
				preLoadSet.add(preLoad.getTextContent().trim());
			}
			servicesConfig.setPreLoads(preLoadSet);
		}
	}

	public void loadPackagesInClasses(Node node) {
		List<Node> packagePaths = getNodes(node.getChildNodes());
		if (!packagePaths.isEmpty()) {
			Set<String> packagePathsSet = new HashSet<>();
			for (Node packagePath : packagePaths) {
				packagePathsSet.add(packagePath.getTextContent().trim());
			}
			servicesConfig.setPackagePaths(packagePathsSet);
		}
	}

	public void loadDatasourceExpressions(Node node) {
		List<Node> dataSourceExpressions = getNodes(node.getChildNodes());
		if (!dataSourceExpressions.isEmpty()) {
			List<Map<String, String>> datasourceExpressionList = new ArrayList<>();
			for (Node datasourceExpressionNode : dataSourceExpressions) {
				Map<String, String> dataSourceExpression = new HashMap<>();
				NamedNodeMap namedNodeMap = datasourceExpressionNode.getAttributes();
				dataSourceExpression.put("id", namedNodeMap.getNamedItem("id").getNodeValue());
				dataSourceExpression.put("latentExpression",
						namedNodeMap.getNamedItem("latent-expression").getNodeValue());
				datasourceExpressionList.add(dataSourceExpression);
			}
			servicesConfig.setDataSourceExpressions(datasourceExpressionList);
		}
	}

	public void loadDecisions(Node node) {
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
			servicesConfig.setDecisions(decisionList);
		}
	}

	public void loadDataSources(Node node) {
		List<Node> dataSources = getNodes(node.getChildNodes());
		if (!dataSources.isEmpty()) {
			for (Node dataSourceNode : dataSources) {
				NamedNodeMap namedNodeMap = dataSourceNode.getAttributes();
				String id = namedNodeMap.getNamedItem("id").getNodeValue();
				List<Node> attributes = getNodes(dataSourceNode.getChildNodes());
				Map<String, Object> attributeMap = new HashMap<>();
				for (Node attribute : attributes) {
					namedNodeMap = attribute.getAttributes();
					String name = namedNodeMap.getNamedItem("name").getNodeValue();
					String content = attribute.getTextContent();
					attributeMap.put(name, content);
				}
				servicesConfig.getDataSources().put(id, attributeMap);
			}
		}
	}

	public ServicesConfigParser() throws Exception {
		String configPath;
		String activeProfile = System.getProperty("dawdler.profiles.active");
		String prefix = "services-conf";
		String suffix = ".xml";
		configPath = (prefix + (activeProfile != null ? "-" + activeProfile : "")) + suffix;
		InputStream xmlInput = DawdlerTool.getResourceFromClassPath(prefix, suffix);
		if (xmlInput == null) {
			throw new IOException("not found " + configPath + " in classPath!");
		}
		InputStream xsdInput = getClass().getResourceAsStream("/services-conf.xsd");
		try {
			parser(xmlInput, xsdInput);
		} finally {
			xmlInput.close();
		}

	}

	private void parser(InputStream xmlInput, InputStream xsdInput) throws Exception {
		servicesConfig = new ServicesConfig();
		Document root = new XmlObject(xmlInput, xsdInput).getDocument();
		List<Node> childNodes = getNodes(root.getDocumentElement().getChildNodes());
		for (Node childNode : childNodes) {
			String childNodeName = childNode.getNodeName();
			if (childNodeName.equals("remote-load")) {
				loadRemoteLoad(childNode);
			} else if (childNodeName.equals("mybatis")) {
				loadMappers(childNode);
			} else if (childNodeName.equals("scanner")) {
				List<Node> scannerChildNodes = getNodes(childNode.getChildNodes());
				for (Node scannerChildNode : scannerChildNodes) {
					String scannerChildNodeName = scannerChildNode.getNodeName();
					if (scannerChildNodeName.equals("loads")) {
						loadPreLoads(scannerChildNode);
					} else if (scannerChildNodeName.equals("package-paths")) {
						loadPackagesInClasses(scannerChildNode);
					}
				}
			} else if (childNodeName.equals("datasource-expressions")) {
				loadDatasourceExpressions(childNode);
			} else if (childNodeName.equals("decisions")) {
				loadDecisions(childNode);
			} else if (childNodeName.equals("datasources")) {
				loadDataSources(childNode);
			}
		}
	}

	public ServicesConfigParser(InputStream xmlInput, InputStream xsdInput) throws Exception {
		parser(xmlInput, xsdInput);
	}

}
