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
package club.dawdler.client.api.generator.util;

import java.math.BigDecimal;

import club.dawdler.client.api.generator.data.ItemsData;
import club.dawdler.client.api.generator.data.MethodParameterData;
import club.dawdler.client.api.generator.data.SchemaData;

/**
 * @author jackson.song
 * @version V1.0
 * CommentUtils
 */
public class CommentUtils {
	public static String getRule(String comment) {
		if (comment == null) {
			return null;
		}
		int index = comment.indexOf("{");
		int end = comment.indexOf("}");
		if (index == -1 || end == -1 || end < index) {
			return null;
		} else {
			return comment.substring(index + 1, end).trim();
		}
	}

	public static void setRule(String commentStr, MethodParameterData parameterData) {
		String[] comment = commentStr.split(" ");
		SchemaData schema = new SchemaData();
		parameterData.setSchema(schema);
		for (int i = 0; i < comment.length; i++) {
			if (i == 0) {
				parameterData.setName(comment[i]);
			} else if (i == 1) {
				parameterData.setDescription(comment[i]);
			} else if (i == 2) {
				String ruleString = CommentUtils.getRule(comment[i]);
				String[] rules = ruleString.split("&");
				for (String rule : rules) {
					if (rule.equals("notEmpty")) {
						parameterData.setRequired(true);
					} else if (rule.startsWith("minLength:")) {
						schema.setMinLength(Integer.parseInt(rule.substring(10)));
					} else if (rule.startsWith("maxLength:")) {
						schema.setMaxLength(Integer.parseInt(rule.substring(10)));
					} else if (rule.startsWith("minimum:")) {
						schema.setMinimum(new BigDecimal(rule.substring(8)));
					} else if (rule.startsWith("maximum:")) {
						schema.setMaximum(new BigDecimal(rule.substring(8)));
					} else if (rule.startsWith("minItems:")) {
						schema.setMinItems(Integer.parseInt(rule.substring(9)));
					} else if (rule.startsWith("maxItems:")) {
						schema.setMaxItems(Integer.parseInt(rule.substring(9)));
					} else if (rule.startsWith("uniqueItems")) {
						schema.setUniqueItems(true);
					}
				}
			}
		}
	}

	public static boolean setRule(String commentStr, SchemaData schema) {
		boolean requiredFlag = false;
		String[] comment = commentStr.split(" ");
		for (int i = 0; i < comment.length; i++) {
			if (i == 0) {
				schema.setDescription(comment[i]);
			} else if (i == 1) {
				String ruleString = CommentUtils.getRule(comment[i]);
				if(ruleString == null){
					continue;
				}
				String[] rules = ruleString.split("&");
				for (String rule : rules) {
					if (rule.equals("notEmpty")) {
						requiredFlag = true;
					} else if (rule.startsWith("minLength:")) {
						schema.setMinLength(Integer.parseInt(rule.substring(10)));
					} else if (rule.startsWith("maxLength:")) {
						schema.setMaxLength(Integer.parseInt(rule.substring(10)));
					} else if (rule.startsWith("minimum:")) {
						schema.setMinimum(new BigDecimal(rule.substring(8)));
					} else if (rule.startsWith("maximum:")) {
						schema.setMaximum(new BigDecimal(rule.substring(8)));
					}
				}
			}
		}
		return requiredFlag;
	}

	public static boolean setRuleForArray(String commentStr, SchemaData schema) {
		boolean requiredFlag = false;
		String[] comment = commentStr.split(" ");
		for (int i = 0; i < comment.length; i++) {
			if (i == 0) {
				schema.setDescription(comment[i]);
			} else if (i == 1) {
				String ruleString = CommentUtils.getRule(comment[i]);
				String[] rules = ruleString.split("&");
				ItemsData itemsData = schema.getItems();
				for (String rule : rules) {
					if (rule.equals("notEmpty")) {
						requiredFlag = true;
					} else if (rule.startsWith("minLength:")) {
						itemsData.setMinLength(Integer.parseInt(rule.substring(10)));
					} else if (rule.startsWith("maxLength:")) {
						itemsData.setMaxLength(Integer.parseInt(rule.substring(10)));
					} else if (rule.startsWith("minimum:")) {
						itemsData.setMinimum(new BigDecimal(rule.substring(8)));
					} else if (rule.startsWith("maximum:")) {
						itemsData.setMaximum(new BigDecimal(rule.substring(8)));
					} else if (rule.startsWith("minItems:")) {
						schema.setMinItems(Integer.parseInt(rule.substring(9)));
					} else if (rule.startsWith("maxItems:")) {
						schema.setMaxItems(Integer.parseInt(rule.substring(9)));
					} else if (rule.startsWith("uniqueItems")) {
						schema.setUniqueItems(true);
					}
				}
			}
		}
		return requiredFlag;
	}

}
