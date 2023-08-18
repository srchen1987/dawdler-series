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
package com.anywide.dawdler.server.service.conf;

import static com.anywide.dawdler.util.XmlTool.getNodes;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.server.service.conf.ServicesConfig.DataSourceExpression;
import com.anywide.dawdler.server.service.conf.ServicesConfig.Decision;
import com.anywide.dawdler.util.XmlTool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServicesConfigParser.java
 * @Description 服务配置解析类 (抛弃老版本的xstream实现使用dom4j实现，最后抛弃dom4j实现)
 * @date 2023年5月1日
 * @email suxuan696@gmail.com
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

	public void loadPackagesInJar(Node node) {
		List<Node> packagesInJar = getNodes(node.getChildNodes());
		if (!packagesInJar.isEmpty()) {
			Set<String> packageInJarSet = new HashSet<>();
			for (Node packageInJar : packagesInJar) {
				packageInJarSet.add(packageInJar.getTextContent().trim());
			}
			servicesConfig.setPackagesInJar(packageInJarSet);
		}
	}

	public void loadPackagesInClasses(Node node) {
		List<Node> packagesInClasses = getNodes(node.getChildNodes());
		if (!packagesInClasses.isEmpty()) {
			Set<String> packageInClassesSet = new HashSet<>();
			for (Node packageInClasses : packagesInClasses) {
				packageInClassesSet.add(packageInClasses.getTextContent().trim());
			}
			servicesConfig.setPackagesInClasses(packageInClassesSet);
		}
	}

	public void loadDatasourceExpressions(Node node) {
		List<Node> dataSourceExpressions = getNodes(node.getChildNodes());
		if (!dataSourceExpressions.isEmpty()) {
			List<DataSourceExpression> datasourceExpressionList = new ArrayList<>();
			for (Node datasourceExpressionNode : dataSourceExpressions) {
				DataSourceExpression dataSourceExpression = servicesConfig.new DataSourceExpression();
				NamedNodeMap namedNodeMap = datasourceExpressionNode.getAttributes();
				dataSourceExpression.setId(namedNodeMap.getNamedItem("id").getNodeValue());
				dataSourceExpression.setLatentExpression(namedNodeMap.getNamedItem("latent-expression").getNodeValue());
				datasourceExpressionList.add(dataSourceExpression);
			}
			servicesConfig.setDataSourceExpressions(datasourceExpressionList);
		}
	}

	public void loadDecisions(Node node) {
		List<Node> decisions = getNodes(node.getChildNodes());
		if (!decisions.isEmpty()) {
			List<Decision> decisionList = new ArrayList<>();
			for (Node decisionNode : decisions) {
				Decision decision = servicesConfig.new Decision();
				NamedNodeMap namedNodeMap = decisionNode.getAttributes();
				decision.setLatentExpressionId(namedNodeMap.getNamedItem("latent-expression-id").getNodeValue());
				decision.setMapping(namedNodeMap.getNamedItem("mapping").getNodeValue());
				decisionList.add(decision);
			}
			servicesConfig.setDecisions(decisionList);
		}
	}

	public ServicesConfigParser(String xmlPath) throws Exception {
		servicesConfig = new ServicesConfig();
		URL url = getClass().getClassLoader().getResource("services-config.xsd");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(url);
		factory.setSchema(schema);
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		docBuilder.setErrorHandler(XmlTool.getErrorHandler());
		Document root = docBuilder.parse(xmlPath);
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
					} else if (scannerChildNodeName.equals("packages-in-jar")) {
						loadPackagesInJar(scannerChildNode);
					} else if (scannerChildNodeName.equals("packages-in-classes")) {
						loadPackagesInClasses(scannerChildNode);
					}
				}
			} else if (childNodeName.equals("datasource-expressions")) {
				loadDatasourceExpressions(childNode);
			} else if (childNodeName.equals("decisions")) {
				loadDecisions(childNode);
			}
		}
	}
	
	
}
