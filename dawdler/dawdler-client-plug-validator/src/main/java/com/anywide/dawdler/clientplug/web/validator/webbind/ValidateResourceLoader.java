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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.clientplug.web.validator.entity.ControlField;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator.MappingFeildType;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.XmlObject;

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
	private static final Map<String, ControlField> globalFields = new HashMap<>();
	private static final ResourceBundle properties;

	static {
		properties = ResourceBundle.getBundle("validate-global-variable");
		try {
			String globalPath = DawdlerTool.getCurrentPath() + "global-validator.xml";
			if (new File(globalPath).isFile()) {
				try {
					XmlObject xmlo = new XmlObject(globalPath);
					List list = xmlo.selectNodes("/global-validator/validator-fields/validator-field");
					for (Object obj : list) {
						Element ele = (Element) obj;
						String name = ele.attributeValue("name");
						String explain = ele.attributeValue("explain");
						String rules = ele.getTextTrim();
						String globalRules = ele.attributeValue("globalRules");
						if (globalRules != null) {
							try {
								String glpro = properties.getString(globalRules);
								if (rules == null || rules.trim().equals("")) {
									rules = glpro;
								} else {
									rules += "&" + glpro;
								}
							} catch (Exception e) {
								logger.warn("not find " + globalRules + " in global properties!");
							}
						}
						globalFields.put(name, new ControlField(name, rules, explain));
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		} catch (MissingResourceException e) {
		}
	}

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

	private static ControlValidator loadRules(InputStream input) throws DocumentException, IOException {
		ControlValidator cv = new ControlValidator();
		XmlObject xml = new XmlObject(input);
		parserFields(xml, cv);
		parserFieldsGroups(xml, cv);
		parserGlobal(xml, cv);
		parserMapping(xml, cv);
		return cv;
	}

	private static void parserGlobal(XmlObject xml, ControlValidator cv) {
		cv.initGlobalControlFieldsCache();
		List<Node> globalList = xml.selectNodes("/validator/global-validator/validator");
		if (globalList != null && !globalList.isEmpty()) {
			for (Node node : globalList) {
				Map<String, ControlField> globals = new LinkedHashMap<String, ControlField>();
				Element ele = (Element) node;
				String refgid = ele.attributeValue("refgid");
				String ref = ele.attributeValue("ref");
				String type = ele.attributeValue("type");
				if (refgid != null) {
					Map<String, ControlField> fieldGroup = cv.getFieldGroups().get(refgid);
					if (fieldGroup != null) {
						globals.putAll(fieldGroup);
					}
				}
				if (ref != null) {
					ControlField confield = cv.getControlFields().get(ref);
					if (confield == null) {
						confield = globalFields.get(ref);
					}
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

	private static void parserFields(XmlObject xml, ControlValidator cv) {
		List<Node> fieldsList = xml.selectNodes("/validator/validator-fields/validator-field");
		if (fieldsList != null) {
			Map<String, ControlField> fields = new LinkedHashMap<String, ControlField>();
			cv.setControlFields(fields);
			for (Node node : fieldsList) {
				Element ele = (Element) node;
				String name = ele.attributeValue("name");
				String explain = ele.attributeValue("explain");
				String rules = ele.getTextTrim();
				String globalRules = ele.attributeValue("globalRules");
				if (globalRules != null) {
					try {
						String glpro = properties.getString(globalRules);
						if (rules.equals("")) {
							rules = glpro;
						} else {
							rules += "&" + glpro;
						}
					} catch (Exception e) {
						logger.warn("not find " + globalRules + " in global properties!");
					}
				}
				fields.put(name, new ControlField(name, rules, explain));
			}
		}
	}

	private static void parserMapping(XmlObject xml, ControlValidator cv) {
		List<Node> mappingList = xml.selectNodes("/validator/validator-mappings/validator-mapping");
		if (mappingList != null && !mappingList.isEmpty()) {
			for (Node mappingNode : mappingList) {
				Element ele = (Element) mappingNode;
				Map<MappingFeildType, Map<String, ControlField>> mappings = new LinkedHashMap<>();
				if (cv.getGlobalControlFields() != null && !cv.getGlobalControlFields().isEmpty()) {
					mappings.putAll(cv.getGlobalControlFields());
				}
				String mname = ele.attributeValue("name");
				String skip = ele.attributeValue("skip");
				List<Node> vlist = ele.selectNodes("validator");
				if (vlist != null) {
					for (Node node : vlist) {
						Element vele = (Element) node;
						String refgid = vele.attributeValue("refgid");
						String ref = vele.attributeValue("ref");
						String type = vele.attributeValue("type");
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
							if (controlField == null) {
								controlField = globalFields.get(ref);
							}
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

	private static void parserFieldsGroups(XmlObject xml, ControlValidator cv) {
		Map<String, ControlField> fields = cv.getControlFields();
		List<Node> groupsList = xml.selectNodes("/validator/validator-fields-groups/validator-fields-group");
		Map<String, Map<String, ControlField>> groups = new LinkedHashMap<String, Map<String, ControlField>>();
		cv.setFieldGroups(groups);
		if (groupsList != null) {
			Map<String, Map<String, String>> relation = new LinkedHashMap<String, Map<String, String>>();
			for (Node groupsNode : groupsList) {
				Element ele = (Element) groupsNode;
				Map<String, ControlField> gfields = new LinkedHashMap<>();
				String gid = ele.attributeValue("id");
				groups.put(gid, gfields);
				List<Node> validatorList = ele.selectNodes("validator");
				if (validatorList != null) {
					for (Node node : validatorList) {
						Element vele = (Element) node;
						String agid = vele.attributeValue("refgid");
						String aref = vele.attributeValue("ref");
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
							if (cf == null) {
								cf = globalFields.get(aref);
							}
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
