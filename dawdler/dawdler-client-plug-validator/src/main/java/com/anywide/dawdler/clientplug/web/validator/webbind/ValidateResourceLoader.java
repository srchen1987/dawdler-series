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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import com.anywide.dawdler.util.XmlObject;

/**
 * 
 * @Title: ValidateResourceLoader.java
 * @Description: 验证文件资源加载器，加载配置文件后配置各种关系
 * @author: jackson.song
 * @date: 2007年07月21日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class ValidateResourceLoader {
	private static Logger logger = LoggerFactory.getLogger(ValidateResourceLoader.class);
	private static Map<String, ControlField> globalFields = new HashMap<String, ControlField>();
	private static ResourceBundle properties = null;
	static {
		properties = ResourceBundle.getBundle("validate_global_variable");
	}
	static {
		try {
			String globalpath = properties.getString("global_path");
			if (globalpath != null) {
				globalpath = globalpath.trim();
				if (!globalpath.equals("")) {
					String classpath = "";
					try {
						classpath = URLDecoder.decode(
								Thread.currentThread().getContextClassLoader().getResource("").getPath(), "utf-8");
					} catch (UnsupportedEncodingException e1) {
					}

					if (globalpath.startsWith("${classpath}"))
						globalpath = globalpath.replace("${classpath}", classpath);
					if (new File(globalpath).isFile())
						try {
							XmlObject xmlo = new XmlObject(globalpath);
							List list = xmlo.selectNodes("/global-validator/validator-fields/validator-field");
							for (Object obj : list) {
								Element ele = (Element) obj;
								String name = ele.attributeValue("name");
								String explain = ele.attributeValue("explain");
//									String rules = ele.attributeValue("rules");
								String rules = ele.getTextTrim();
								String globalrules = ele.attributeValue("globalrules");
								if (globalrules != null) {
									try {
										String glpro = properties.getString(globalrules);
										if (rules == null || rules.trim().equals(""))
											rules = glpro;
										else
											rules += "&" + glpro;
									} catch (Exception e) {
										logger.warn("not find " + globalrules + " in global properties!");
									}
//										if(glpro==null){
//											logger.warn("not find "+globalrules+" in global properties!");
//										}else{
//											//if(rules==null)
//											if(rules.equals(""))
//											rules = glpro;
//											else
//											rules+="&"+glpro;
//										}
								}
								globalFields.put(name, new ControlField(name, rules, explain));
							}
						} catch (Exception e) {
							logger.error("", e);
						}
				}
			}
		} catch (MissingResourceException e) {
		}
	}

	public static ControlValidator getControlValidator(Class controlClass) {
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
		cv.setValidate(true);
		XmlObject xml = new XmlObject(input);
		parserFields(xml, cv);
		parserFieldsGroups(xml, cv);
		parserGlobal(xml, cv);
		parserMapping(xml, cv);
		return cv;
	}

	private static void parserGlobal(XmlObject xml, ControlValidator cv) {
		List<Element> globallist = (List<Element>) xml.selectNodes("/validator/global-validator/validator");
		if (globallist != null && !globallist.isEmpty()) {
			Map<String, ControlField> globals = new LinkedHashMap<String, ControlField>();
			for (Element ele : globallist) {
				String refgid = ele.attributeValue("refgid");
				String ref = ele.attributeValue("ref");
				if (refgid != null) {
					Map map = cv.getFieldGroups().get(refgid);
					if (map != null)
						globals.putAll(map);
				}
				if (ref != null) {
					ControlField confield = cv.getControlFields().get(ref);
					if (confield == null) {
						confield = globalFields.get(ref);
					}
					if (confield != null)
						globals.put(confield.getFieldName(), confield);
					else
						logger.warn("can't find " + ref + " in fields!");
				}
			}
			cv.setGlobalControlFields(globals);
		}
	}

	private static void parserFields(XmlObject xml, ControlValidator cv) {
		List<Element> fieldslist = (List<Element>) xml.selectNodes("/validator/validator-fields/validator-field");
		if (fieldslist != null) {
			Map<String, ControlField> fields = new LinkedHashMap<String, ControlField>();
			cv.setControlFields(fields);
			for (Element ele : fieldslist) {
				String name = ele.attributeValue("name");
				String explain = ele.attributeValue("explain");
//				String rules = ele.attributeValue("rules");
				String rules = ele.getTextTrim();
				String globalrules = ele.attributeValue("globalrules");
				if (globalrules != null) {
					try {
						String glpro = properties.getString(globalrules);
						if (rules.equals(""))
							rules = glpro;
						else
							rules += "&" + glpro;
					} catch (Exception e) {
						logger.warn("not find " + globalrules + " in global properties!");
					}
					/*
					 * String glpro = cproperty.getString(globalrules); if(glpro==null){
					 * logger.warn("not find "+globalrules+" in global properties!"); }else{ //
					 * if(rules==null) if(rules.equals("")) rules = glpro; else rules+="&"+glpro; }
					 */
				}
				fields.put(name, new ControlField(name, rules, explain));
			}
		}
	}

	private static void parserMapping(XmlObject xml, ControlValidator cv) {
		List<Element> mappinglist = (List<Element>) xml.selectNodes("/validator/validator-mappings/validator-mapping");
		if (mappinglist != null && !mappinglist.isEmpty()) {
			for (Element ele : mappinglist) {
				Map<String, ControlField> mappings = new LinkedHashMap<String, ControlField>();
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
						if (refgid != null) {
							Map map = cv.getFieldGroups().get(refgid);
							if (map != null)
								mappings.putAll(map);
						}
						if (ref != null) {
							ControlField confield = cv.getControlFields().get(ref);
							if (confield == null) {
								confield = globalFields.get(ref);
							}
							if (confield != null)
								mappings.put(confield.getFieldName(), confield);
							else
								logger.warn("can't find " + ref + " in fields!");
						}
					}
				}
				if (skip != null && !skip.trim().equals("")) {
					String[] skips = skip.split(",");
					for (String s : skips) {
						mappings.remove(s);
					}
				}
				cv.getMappings().put(mname, mappings);
			}
		}
	}

	private static void parserFieldsGroups(XmlObject xml, ControlValidator cv) {
		Map<String, ControlField> fields = cv.getControlFields();
		List<Element> groupslist = (List<Element>) xml.selectNodes("/validator/validator-fields-groups/validator-fields-group");
		Map<String, Map<String, ControlField>> groups = new LinkedHashMap<String, Map<String, ControlField>>();
		cv.setFieldGroups(groups);
		if (groupslist != null) {
			Map<String, Map<String, String>> relation = new LinkedHashMap<String, Map<String, String>>();
			for (Element ele : groupslist) {
				Map gfields = new LinkedHashMap<String, ControlField>();
				String gid = ele.attributeValue("id");
				groups.put(gid, gfields);
				List<Node> validatorList = ele.selectNodes("validator");
				if (validatorList != null) {
					for (Node node : validatorList) {
						Element vele = (Element) node;
						String agid = vele.attributeValue("refgid");
						String aref = vele.attributeValue("ref");
						if (agid != null) {
							Map rmap = relation.get(gid);
							if (rmap == null) {
								rmap = new LinkedHashMap();
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
			Map<String, Map<String, String>> relation, Set locations, Map<String, ControlField> additive) {
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
		if (additive == null)
			additive = group;
		else
			additive.putAll(group);
		if (dependents == null || dependents.isEmpty())
			return;
		Set<Entry<String, String>> entryset = dependents.entrySet();
		for (Entry<String, String> entry : entryset) {
			groupsOperation(entry.getKey(), groups, relation, locations, additive);
		}
	}
}
