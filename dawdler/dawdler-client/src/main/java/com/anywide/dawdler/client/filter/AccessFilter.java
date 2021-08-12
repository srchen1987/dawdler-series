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
package com.anywide.dawdler.client.filter;

import com.anywide.dawdler.core.bean.RequestBean;

/**
 * @author jackson.song
 * @version V1.0
 * @Title AccessFilter.java
 * @Description 一个前端Filter的演示类 //需要通过SPI配置 此类只是空实现 无用
 * @date 2015年4月06日
 * @email suxuan696@gmail.com
 */
public class AccessFilter implements DawdlerClientFilter {
	@Override
	public Object doFilter(RequestBean request, FilterChain chain) throws Exception {
		return chain.doFilter(request);
	}

}
