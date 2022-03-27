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
package com.anywide.dawdler.client.api.generator.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author jackson.song
 * @version V1.0
 * @Title MethodParameterData.java
 * @Description MethodParameterData
 * @date 2022年3月19日
 * @email suxuan696@gmail.com
 */
public class MethodParameterData {

	@JsonInclude(Include.NON_NULL)
	private String name;

	@JsonInclude(Include.NON_NULL)
	private String type;

	@JsonInclude(Include.NON_NULL)
	private String in;

	@JsonInclude(Include.NON_NULL)
	private String description;

	@JsonInclude(Include.NON_NULL)
	private Boolean required;

	@JsonInclude(Include.NON_NULL)
	private String format;

	@JsonInclude(Include.NON_NULL)
	private SchemaData schema;

	@JsonInclude(Include.NON_NULL)
	private ItemsData items;

	@JsonInclude(Include.NON_NULL)
	private String collectionFormat;

	@JsonInclude(Include.NON_NULL)
	private Boolean allowEmptyValue;

	public String getCollectionFormat() {
		return collectionFormat;
	}

	public void setCollectionFormat(String collectionFormat) {
		this.collectionFormat = collectionFormat;
	}

	public Boolean getAllowEmptyValue() {
		return allowEmptyValue;
	}

	public void setAllowEmptyValue(Boolean allowEmptyValue) {
		this.allowEmptyValue = allowEmptyValue;
	}

	public ItemsData getItems() {
		return items;
	}

	public void setItems(ItemsData items) {
		this.items = items;
	}

	public SchemaData getSchema() {
		return schema;
	}

	public void setSchema(SchemaData schema) {
		this.schema = schema;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}

	@Override
	public String toString() {
		return name + ":" + type + ":" + format + ":" + description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
