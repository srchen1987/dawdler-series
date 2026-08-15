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
package club.dawdler.i18n;

import java.util.Locale;

/**
 * @author jackson.song
 * @version V1.0
 * 国际化操作接口 提供获取国际化资源的方法，支持根据当前语言环境或指定语言环境获取对应的资源值。
 */
public interface I18nOperator {

	/**
	 * 根据资源键获取当前语言环境下的资源值
	 * 
	 * @param key 资源键
	 * @return 对应语言环境下的资源值
	 */
	String get(String key);

	/**
	 * 根据资源键和指定语言环境获取资源值
	 * 
	 * @param key    资源键
	 * @param locale 指定的语言环境
	 * @return 对应语言环境下的资源值
	 */
	String get(String key, Locale locale);

}