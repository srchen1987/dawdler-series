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

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jackson.song
 * @version V1.0
 * SchemaData
 */
public class SchemaData {
	@JsonInclude(Include.NON_NULL)
	private String type;
	@JsonInclude(Include.NON_NULL)
	private String format;
	@JsonInclude(Include.NON_NULL)
	private ItemsData items;
	@JsonInclude(Include.NON_NULL)
	private String $ref;

	@JsonInclude(Include.NON_NULL)
	private Integer minLength;

	@JsonInclude(Include.NON_NULL)
	private Integer maxLength;

	@JsonInclude(Include.NON_NULL)
	private BigDecimal minimum;

	@JsonInclude(Include.NON_NULL)
	private BigDecimal maximum;

	@JsonInclude(Include.NON_NULL)
	private Integer minItems;

	@JsonInclude(Include.NON_NULL)
	private Integer maxItems;

	@JsonInclude(Include.NON_NULL)
	private Boolean uniqueItems;

	@JsonInclude(Include.NON_NULL)
	@JsonProperty("default")
	private String defaultText;

	@JsonInclude(Include.NON_NULL)
	private String description;

	@JsonInclude(Include.NON_NULL)
	private String style;

	@JsonInclude(Include.NON_NULL)
	private Boolean explode;

	@JsonInclude(Include.NON_NULL)
	@JsonProperty("enum")
	private List<String> enumList;
	public String get$ref() {
		return $ref;
	}

	public void set$ref(String $ref) {
		this.$ref = $ref.replaceAll("<", "_").replaceAll(">", "_");
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ItemsData getItems() {
		return items;
	}

	public void setItems(ItemsData items) {
		this.items = items;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public BigDecimal getMinimum() {
		return minimum;
	}

	public BigDecimal getMaximum() {
		return maximum;
	}

	public void setMinimum(BigDecimal minimum) {
		this.minimum = minimum;
	}

	public void setMaximum(BigDecimal maximum) {
		this.maximum = maximum;
	}

	public Integer getMinItems() {
		return minItems;
	}

	public void setMinItems(Integer minItems) {
		this.minItems = minItems;
	}

	public Integer getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(Integer maxItems) {
		this.maxItems = maxItems;
	}

	public String getDefaultText() {
		return defaultText;
	}

	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getUniqueItems() {
		return uniqueItems;
	}

	public void setUniqueItems(Boolean uniqueItems) {
		this.uniqueItems = uniqueItems;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public Boolean getExplode() {
		return explode;
	}

	public void setExplode(Boolean explode) {
		this.explode = explode;
	}

	public List<String> getEnumList() {
		return enumList;
	}

	public void setEnumList(List<String> enumList) {
		this.enumList = enumList;
	}

}
