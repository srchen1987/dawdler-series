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
package com.anywide.dawdler.client.api.generator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.anywide.dawdler.client.api.generator.conf.OpenApiConfig;
import com.anywide.dawdler.client.api.generator.data.ClassStruct;
import com.anywide.dawdler.client.api.generator.data.ControllerData;
import com.anywide.dawdler.clientplug.annotation.Controller;
import com.anywide.dawdler.clientplug.annotation.RequestMapping;
import com.anywide.dawdler.util.JsonProcessUtil;
import com.anywide.dawdler.util.YAMLMapperFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * @author jackson.song
 * @version V1.0
 * @Title WebApiGenerator.java
 * @Description webApi生成器
 * @date 2022年3月20日
 * @email suxuan696@gmail.com
 */
public class WebApiGenerator {

	public static void generate(File file) throws IOException {
		YAMLMapper yamlMapper = YAMLMapperFactory.getYAMLMapper();
		OpenApiConfig openApi = yamlMapper.readValue(file, OpenApiConfig.class);
		Map<String, ClassStruct> classStructs = new HashMap<>();
		JavaProjectBuilder javaProjectBuilder = new JavaProjectBuilder();
		List<File> listList = new ArrayList<>();
		for (String filePath : openApi.getScanPath()) {
			addPath(new File(filePath), listList);
		}
		for (File javaFile : listList) {
			javaProjectBuilder.addSource(javaFile);
		}
		Map<String, Object> rootMap = new LinkedHashMap<String, Object>();
		Map<String, Object> infoMap = new LinkedHashMap<String, Object>();
		Map<String, Object> definitionsMap = new LinkedHashMap<String, Object>();
		Map<String, Object> pathMap = new LinkedHashMap<String, Object>();
		infoMap.put("version", openApi.getVersion());
		infoMap.put("title", openApi.getTitle());
		infoMap.put("description", openApi.getDescription());
		infoMap.put("contact", openApi.getContact());

		rootMap.put("swagger", openApi.getSwagger());
		rootMap.put("info", infoMap);
		rootMap.put("host", openApi.getHost());
		rootMap.put("basePath", openApi.getBasePath());
		rootMap.put("paths", pathMap);
		List<ControllerData> controllers = new ArrayList<>(128);
		rootMap.put("tags", controllers);

		Collection<JavaClass> classes = javaProjectBuilder.getClasses();
		Collection<JavaSource> sources = javaProjectBuilder.getSources();

		for (JavaSource source : sources) {
			ClassStruct struct = new ClassStruct();
			struct.setImportPackages(source.getImports());
			struct.setJavaClass(source.getClasses().get(0));
			classStructs.put(source.getClasses().get(0).getBinaryName(), struct);
		}

		for (JavaClass javaClass : classes) {
			String className = javaClass.getBinaryName();
			if (className.contains("$")) {
				String superClassName = className.substring(0, className.lastIndexOf("$"));
				List<String> imports = classStructs.get(superClassName).getImportPackages();
				ClassStruct struct = new ClassStruct();
				struct.setImportPackages(imports);
				struct.setJavaClass(javaClass);
				classStructs.put(className, struct);
			}
			boolean isController = false;
			JavaAnnotation requsetMappingAnnotation = null;
			if (!javaClass.isInner() && !javaClass.isInterface() && !javaClass.isEnum() && !javaClass.isAbstract()
					&& !javaClass.isAnnotation()) {
				List<JavaAnnotation> classAnnotations = javaClass.getAnnotations();
				for (JavaAnnotation classAnnotation : classAnnotations) {
					if (Controller.class.getName().equals(classAnnotation.getType().getBinaryName())) {
						isController = true;
					}
					if (RequestMapping.class.getName().equals(classAnnotation.getType().getBinaryName())) {
						requsetMappingAnnotation = classAnnotation;
					}
				}
			}
			if (isController) {
				ControllerData data = new ControllerData();
				DocletTag docletTag = javaClass.getTagByName("Description");
				String describe;
				if (docletTag != null) {
					describe = docletTag.getValue();
				} else {
					describe = javaClass.getBinaryName();
				}
				data.setName(javaClass.getBinaryName());
				data.setDescription(describe);
				controllers.add(data);
				MethodParser.generateMethodParamCode(rootMap, pathMap, classStructs, definitionsMap, javaClass,
						requsetMappingAnnotation);
			}

		}
		rootMap.put("definitions", definitionsMap);
		String openApiText = JsonProcessUtil.beanToJson(rootMap);
		OutputStream out = new FileOutputStream(openApi.getOutPath());
		out.write(openApiText.getBytes());
		out.flush();
		out.close();
	}

	public static void generate(String configPath) throws IOException {
		File file = new File(configPath);
		generate(file);

	}

	public static void addPath(File file, List<File> list) {
		if (file.isDirectory()) {
			File[] files = file.listFiles(new FileFilter() {
				public boolean accept(File pathName) {
					return pathName.isDirectory() || pathName.getName().endsWith(".java");
				}
			});
			for (File f : files) {
				addPath(f, list);
			}
		} else {
			list.add(file);
		}
	}

}
