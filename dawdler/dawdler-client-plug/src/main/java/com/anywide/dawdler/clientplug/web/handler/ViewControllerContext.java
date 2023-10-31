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
package com.anywide.dawdler.clientplug.web.handler;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ViewControllerContext.java
 * @Description view层的上下层
 * @date 2007年4月17日
 * @email suxuan696@gmail.com
 */
public class ViewControllerContext {
	private ViewControllerContext() {}
	private static final ThreadLocal<ViewForward> VIEWFORWARD = new ThreadLocal<>();

	public static ViewForward getViewForward() {
		return VIEWFORWARD.get();
	}

	static void setViewForward(ViewForward vd) {
		VIEWFORWARD.set(vd);
	}

	public static void removeViewForward() {
		VIEWFORWARD.remove();
	}

}
