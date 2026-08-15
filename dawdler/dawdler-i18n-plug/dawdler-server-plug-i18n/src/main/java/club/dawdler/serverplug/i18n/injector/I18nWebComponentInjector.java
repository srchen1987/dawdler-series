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
package club.dawdler.serverplug.i18n.injector;

import club.dawdler.core.component.injector.CustomComponentInjector;

/**
 * @author jackson.song
 * @version V1.0
 * 国际化组件注入器 用于扫描和注入国际化相关的组件
 */
public class I18nWebComponentInjector implements CustomComponentInjector {

	@Override
	public String[] scanLocationsForAllInjector() {
		return new String[] { "club.dawdler.serverplug.i18n.filter" };
	}

	@Override
	public boolean useAop() {
		return false;
	}

}
