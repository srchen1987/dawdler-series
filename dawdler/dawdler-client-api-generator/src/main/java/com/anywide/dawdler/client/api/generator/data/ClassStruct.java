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

import java.util.List;

import com.thoughtworks.qdox.model.JavaClass;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClassStruct.java
 * @Description ClassStruct存储结构,存储导入的包
 * @date 2022年3月19日
 * @email suxuan696@gmail.com
 */
public class ClassStruct {
	private List<String> importPackages;
	private JavaClass javaClass;

	public JavaClass getJavaClass() {
		return javaClass;
	}

	public void setJavaClass(JavaClass javaClass) {
		this.javaClass = javaClass;
	}

	public List<String> getImportPackages() {
		return importPackages;
	}

	public void setImportPackages(List<String> importPackages) {
		this.importPackages = importPackages;
	}

}
