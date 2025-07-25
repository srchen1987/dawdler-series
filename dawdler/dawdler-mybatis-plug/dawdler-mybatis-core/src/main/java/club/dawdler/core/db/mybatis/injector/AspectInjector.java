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
package club.dawdler.core.db.mybatis.injector;

import org.apache.ibatis.session.SqlSession;

import club.dawdler.core.annotation.Order;
import club.dawdler.core.component.injector.CustomComponentInjector;

/**
 * @author jackson.song
 * @version V1.0
 * 加载需要被aspect织入的组件
 */
@Order(0)
public class AspectInjector implements CustomComponentInjector {

	@Override
	public Class<?>[] getMatchTypes() {
		return new Class[] { SqlSession.class };
	}

	@Override
	public String[] scanLocations() {
		return new String[] { "org.apache.ibatis.session.defaults" };
	}

	@Override
	public boolean isInject() {
		return false;
	}

}
