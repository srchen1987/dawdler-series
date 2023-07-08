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
package com.anywide.dawdler.clientplug.web.validator.webbind;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.anywide.dawdler.clientplug.web.validator.entity.ControlField;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator.MappingFeildType;
import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.XmlTool;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ValidateResourceLoader.java
 * @Description 验证文件资源加载器，加载配置文件后配置各种关系
 * @date 2007年7月21日
 * @email suxuan696@gmail.com
 */
public class ValidateResourceLoader {
	private static final Logger logger = LoggerFactory.getLogger(ValidateResourceLoader.class);

	public static ControlValidator getControlValidator(Class<?> controlClass) {
		InputStream input = controlClass.getResourceAsStream(controlClass.getSimpleName() + "-validator.xml");
		if (input != null) {
			try {
				return loadRules(input);
			} catch (Exception e) {
				logger.error("", e);
				return null;
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return null;
	}

	private static ControlValidator loadRules(InputStream input) throws Exception {
		ControlValidator cv = new ControlValidator();
		URL url = ValidateResourceLoader.class.getClassLoader().getResource("controller-validator.xsd");
		XmlObject xmlObject = new XmlObject(input, url);
		xmlObject.setPrefix("ns");
		parserFields(xmlObject, cv);
		parserFieldsGroups(xmlObject, cv);
		parserGlobal(xmlObject, cv);
		parserMapping(xmlObject, cv);
		return cv;
	}

	private static void parserGlobal(XmlObject xmlObject, ControlValidator cv) throws XPathExpressionException {
		cv.initGlobalControlFieldsCache();
		List<Node> globalList = xmlObject.selectNodes("/ns:validator/ns:global-validator/ns:validator");
		if (globalList != null && !globalList.isEmpty()) {
			for (Node node : globalList) {
				Map<String, ControlField> globals = new LinkedHashMap<String, ControlField>();
				NamedNodeMap namedNodeMap = node.getAttributes();
				String refgid = XmlTool.getElementAttribute(namedNodeMap, "refgid");
				String ref = XmlTool.getElementAttribute(namedNodeMap, "ref");
				String type = XmlTool.getElementAttribute(namedNodeMap, "type");
				if (refgid != null) {
					Map<String, ControlField> fieldGroup = cv.getFieldGroups().get(refgid);
					if (fieldGroup != null) {
						globals.putAll(fieldGroup);
					}
				}
				if (ref != null) {
					ControlField confield = cv.getControlFields().get(ref);
					if (confield != null) {
						globals.put(confield.getFieldName(), confield);
					} else {
						logger.warn("can't find " + ref + " in fields!");
					}
				}
				cv.addGlobalControlFields(ControlValidator.getMappingFeildType(type), globals);
			}

		}
	}

	private static void parserFields(XmlObject xmlObject, ControlValidator cv) throws XPathExpressionException {
		List<Node> fieldsList = xmlObject.selectNodes("/ns:validator/ns:validator-fields/ns:validator-field");
		if (fieldsList != null) {
			Map<String, ControlField> fields = new LinkedHashMap<String, ControlField>();
			cv.setControlFields(fields);
			for (Node node : fieldsList) {
				NamedNodeMap namedNodeMap = node.getAttributes();
				String name = XmlTool.getElementAttribute(namedNodeMap, "name");
				String explain = XmlTool.getElementAttribute(namedNodeMap, "explain");
				String rules = node.getTextContent().trim();
				fields.put(name, new ControlField(name, rules, explain));
			}
		}
	}

	private static void parserMapping(XmlObject xmlObject, ControlValidator cv) throws XPathExpressionException {
		List<Node> mappingList = xmlObject.selectNodes("/ns:validator/ns:validator-mappings/ns:validator-mapping");
		if (mappingList != null && !mappingList.isEmpty()) {
			for (Node mappingNode : mappingList) {
				NamedNodeMap namedNodeMap = mappingNode.getAttributes();
				Map<MappingFeildType, Map<String, ControlField>> mappings = new LinkedHashMap<>();
				if (cv.getGlobalControlFields() != null && !cv.getGlobalControlFields().isEmpty()) {
					mappings.putAll(cv.getGlobalControlFields());
				}
				String mname = XmlTool.getElementAttribute(namedNodeMap, "name");
				String skip = XmlTool.getElementAttribute(namedNodeMap, "skip");
				List<Node> vlist = xmlObject
						.selectNodes("/ns:validator/ns:validator-mappings/ns:validator-mapping/ns:validator");
				if (vlist != null) {
					for (Node validatorNode : vlist) {
						NamedNodeMap validatorNamedNodeMap = validatorNode.getAttributes();
						String refgid = XmlTool.getElementAttribute(validatorNamedNodeMap, "refgid");
						String ref = XmlTool.getElementAttribute(validatorNamedNodeMap, "ref");
						String type = XmlTool.getElementAttribute(validatorNamedNodeMap, "type");
						MappingFeildType mappingFeildType = ControlValidator.getMappingFeildType(type);
						if (refgid != null) {
							Map<String, ControlField> fieldGroup = cv.getFieldGroups().get(refgid);
							if (fieldGroup != null) {
								Map<String, ControlField> fields = mappings.get(mappingFeildType);
								if (fields != null) {
									fields.putAll(fieldGroup);
								} else {
									mappings.put(mappingFeildType, fieldGroup);
								}
							}
						}
						if (ref != null) {
							ControlField controlField = cv.getControlFields().get(ref);
							if (controlField != null) {
								Map<String, ControlField> fields = mappings.get(mappingFeildType);
								if (fields == null) {
									fields = new LinkedHashMap<>();
								}
								fields.put(controlField.getFieldName(), controlField);
								mappings.put(mappingFeildType, fields);
							} else {
								logger.warn("can't find " + ref + " in fields!");
							}
						}
					}
				}
				if (skip != null && !skip.trim().equals("")) {
					String[] skips = skip.split(",");
					for (String skipField : skips) {
						mappings.forEach((k, v) -> {
							v.remove(skipField);
						});
					}
				}
				cv.getMappings().put(mname, mappings);
			}
		}
	}

	private static void parserFieldsGroups(XmlObject xmlObject, ControlValidator cv) throws XPathExpressionException {
		Map<String, ControlField> fields = cv.getControlFields();
		List<Node> groupsList = xmlObject
				.selectNodes("/ns:validator/ns:validator-fields-groups/ns:validator-fields-group");
		Map<String, Map<String, ControlField>> groups = new LinkedHashMap<String, Map<String, ControlField>>();
		cv.setFieldGroups(groups);
		if (groupsList != null) {
			Map<String, Map<String, String>> relation = new LinkedHashMap<String, Map<String, String>>();
			for (Node groupsNode : groupsList) {
				Map<String, ControlField> gfields = new LinkedHashMap<>();
				String gid = XmlTool.getElementAttribute(groupsNode.getAttributes(), "id");
				groups.put(gid, gfields);
				List<Node> validatorList = xmlObject
						.selectNodes("/ns:validator/ns:validator-fields-groups/ns:validator-fields-group/ns:validator");
				if (validatorList != null) {
					for (Node node : validatorList) {
						NamedNodeMap namedNodeMap = node.getAttributes();
						String agid = XmlTool.getElementAttribute(namedNodeMap, "refgid");
						String aref = XmlTool.getElementAttribute(namedNodeMap, "ref");
						if (agid != null) {
							Map<String, String> rmap = relation.get(gid);
							if (rmap == null) {
								rmap = new LinkedHashMap<>();
								relation.put(gid, rmap);
							}
							rmap.put(agid, gid);
						}
						if (aref != null) {
							ControlField cf = fields.get(aref);
							if (cf != null) {
								gfields.put(cf.getFieldName(), cf);
							} else {
								logger.warn("can't find " + aref + " in fields!");
							}
						}
					}
				}
			}
			Set<String> rset = relation.keySet();
			for (String gid : rset) {
				Set<String> locations = new HashSet<String>();
				groupsOperation(gid, groups, relation, locations, null);
			}
		}
	}

	private static void groupsOperation(String gid, Map<String, Map<String, ControlField>> groups,
			Map<String, Map<String, String>> relation, Set<String> locations, Map<String, ControlField> additive) {
		Map<String, String> dependents = relation.get(gid);
		if (locations.contains(gid)) {
			logger.error("", gid + " have relation too many in " + locations.toString());
			return;
		}
		locations.add(gid);
		Map<String, ControlField> group = groups.get(gid);
		if (group == null || group.isEmpty()) {
			logger.error("", "can't find gid = " + gid);
			return;
		}
		if (additive == null) {
			additive = group;
		} else {
			additive.putAll(group);
		}
		if (dependents == null || dependents.isEmpty()) {
			return;
		}
		Set<Entry<String, String>> entryset = dependents.entrySet();
		for (Entry<String, String> entry : entryset) {
			groupsOperation(entry.getKey(), groups, relation, locations, additive);
		}
	}
}
