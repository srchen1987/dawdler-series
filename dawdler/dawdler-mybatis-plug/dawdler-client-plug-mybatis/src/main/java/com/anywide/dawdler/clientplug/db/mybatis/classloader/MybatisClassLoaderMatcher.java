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
package com.anywide.dawdler.clientplug.db.mybatis.classloader;

import com.anywide.dawdler.clientplug.web.classloader.DawdlerClassLoaderMatcher;

/**
 * @author jackson.song
 * @version V1.0
 * mybatis匹配器 针对部署在web容器中的类做Dawdler自定义类加载匹配
 */
public class MybatisClassLoaderMatcher implements DawdlerClassLoaderMatcher{

	@Override
	public String[] matchPackageName() {
		return new String[] {"org.apache.ibatis","com.anywide.dawdler.core.db.mybatis","com.anywide.dawdler.clientplug.db.mybatis"};
	}

}
