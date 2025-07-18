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
package club.dawdler.core.scan;

import java.io.IOException;

import club.dawdler.util.spring.antpath.PathMatchingResourcePatternResolver;
import club.dawdler.util.spring.antpath.Resource;
import club.dawdler.util.spring.antpath.ResourcePatternResolver;

/**
 * @author jackson.song
 * @version V1.0
 * 类扫描器,用于替换DeployClassesScanner与验证框架中的这类实现
 */
public class DawdlerComponentScanner {
	private static ResourcePatternResolver resolver = PathMatchingResourcePatternResolver.getInstance();

	public static Resource getClass(String location) {
		return resolver.getResource(location.replace(".", "/").concat(".class"));
	}

	public static Resource[] getClasses(String location) throws IOException {
		return resolver.getResources("classpath*:" + location.replace(".", "/") + "/*.class");
	}

}
