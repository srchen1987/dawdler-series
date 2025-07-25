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
package club.dawdler.client.api.generator.data;

import java.util.Map;

import club.dawdler.client.api.generator.FieldParser;
import club.dawdler.client.api.generator.TypesConverter;
import club.dawdler.client.api.generator.TypesConverter.TypeData;
import club.dawdler.client.api.generator.util.ClassTypeUtil;
import com.thoughtworks.qdox.model.JavaType;

/**
 * @author jackson.song
 * @version V1.0
 * ParserTypeData
 */
public class ParserTypeData {

	public static void convertion(JavaType javaType, MethodParameterData parameterData,
			Map<String, ClassStruct> classStructs, Map<String, MethodParameterData> params, boolean isArray) {
		String typeName = javaType.getFullyQualifiedName();
		boolean typeArray = typeName.endsWith("[]");
		boolean collection = ClassTypeUtil.isArray(javaType.getBinaryName());
		String type = null;
		if (typeArray || isArray || collection) {
			if (typeArray) {
				type = typeName.substring(0, typeName.lastIndexOf("[]"));
			} else if (collection) {
				type = ClassTypeUtil.getType0(javaType);
			} else {
				type = typeName;
			}
			parameterData.setType("array");
			TypeData typeData = TypesConverter.getType(type);
			ItemsData items = new ItemsData();
			if (typeData != null) {
				items.setType(typeData.getType());
				items.setFormat(typeData.getFormat());
			} else {
				ClassStruct classStruct = classStructs.get(type);
				if (classStruct != null) {
					if (params != null) {
						params.remove(parameterData.getName());
					}
					FieldParser.parserFields(classStruct.getJavaClass(), classStructs, params, true);
				}
			}
//			csv (default)	Comma-separated values.	foo,bar,baz
//			ssv	Space-separated values.	foo bar baz
//			tsv	Tab-separated values.	"foo\tbar\tbaz"
//			pipes	Pipe-separated values.	foo|bar|baz
//			multi	Multiple parameter instances rather than multiple values. This is only supported for the in: query and in: formData parameters.	foo=value&foo=another_value
// only support multi			
			parameterData.setCollectionFormat("multi");
			parameterData.setAllowEmptyValue(false);
			parameterData.setItems(items);
		} else {
			TypeData typeData = TypesConverter.getType(typeName);
			if (typeData != null) {
				parameterData.setType(typeData.getType());
				parameterData.setFormat(typeData.getFormat());
			} else {
				if (params != null) {
					params.remove(parameterData.getName());
				}
				ClassStruct classStruct = classStructs.get(typeName);
				if (classStruct != null) {
					FieldParser.parserFields(classStruct.getJavaClass(), classStructs, params, false);
				}
			}
		}
	}

}
