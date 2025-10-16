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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author jackson.song
 * @version V1.0
 * ResponseData
 */
public class ResponseData {
	private String description;
	@JsonInclude(Include.NON_NULL)
	private Map<String, Map<String, SchemaData>> content;

	public ResponseData(String description) {
		this.description = description;
	}

	public ResponseData(String description, Map<String, Map<String, SchemaData>> content) {
		this.description = description;
		this.content = content;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, Map<String, SchemaData>> getContent() {
		return content;
	}

}
