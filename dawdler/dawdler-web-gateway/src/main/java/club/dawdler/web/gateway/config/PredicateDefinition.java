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
package club.dawdler.web.gateway.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 路由谓词定义，解析 Name=Args 形式的谓词表达式。
 */
public class PredicateDefinition {

	private String name;
	private String args;

	public PredicateDefinition() {
	}

	@JsonCreator(mode = Mode.DELEGATING)
	public PredicateDefinition(String expression) {
		int idx = expression.indexOf('=');
		if (idx > 0) {
			this.name = expression.substring(0, idx).trim();
			this.args = expression.substring(idx + 1).trim();
		} else {
			this.name = expression.trim();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

}
