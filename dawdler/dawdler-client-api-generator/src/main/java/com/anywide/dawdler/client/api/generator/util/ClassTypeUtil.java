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
package com.anywide.dawdler.client.api.generator.util;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ClassTypeUtil.java
 * @Description 类型操作工具类
 * @date 2022年3月20日
 * @email suxuan696@gmail.com
 */
public class ClassTypeUtil {

	/**
	 * 
	 * @Title: isArray
	 * @author jackson.song
	 * @date 2022年3月27日 下午1:41:02
	 * @Description 是否是List,Set,Collection,Vector
	 * @param binaryName
	 * @return
	 *
	 * 
	 */
	public static boolean isArray(String binaryName) {
		return binaryName.equals("java.util.List") || binaryName.equals("java.util.Set")
				|| binaryName.equals("java.util.Collection") || binaryName.equals("java.util.Vector");
	}

}
