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
package com.anywide.dawdler.distributed.transaction.server.listener;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.distributed.transaction.release.ResourceReleaser;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.listener.DawdlerServiceListener;

/**
 *
 * @Title DawdlerListener2ReleaseResources.java
 * @Description dawdler容器端释放资源的监听器
 * @author jackson.song
 * @date 2021年4月17日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
@Order(Integer.MAX_VALUE - 1)
public class DawdlerListener2ReleaseResources implements DawdlerServiceListener {

	@Override
	public void contextInitialized(DawdlerContext dawdlerContext) throws Exception {
	}

	@Override
	public void contextDestroyed(DawdlerContext dawdlerContext) throws Exception {
		ResourceReleaser.release();
	}

}
