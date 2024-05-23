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
package com.navercorp.pinpoint.plugin.dawdler;

import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jackson.song
 * @version V1.0
 * pinpoint中dawdler配置
 */
public class DawdlerConfiguration {

	private final boolean dawdlerEnabled;

	private final List<String> dawdlerBootstrapMains;

	public DawdlerConfiguration(ProfilerConfig config) {
		this.dawdlerEnabled = config.readBoolean("profiler.dawdler.enable", true);
		this.dawdlerBootstrapMains = config.readList("profiler.dawdler.bootstrap.main");
	}

	public boolean isDawdlerEnabled() {
		return dawdlerEnabled;
	}

	public List<String> getDawdlerBootstrapMains() {
		return dawdlerBootstrapMains;
	}

	@Override
	public String toString() {
		return "DawdlerConfiguration{" + "dawdlerEnabled=" + dawdlerEnabled + ", dawdlerBootstrapMains="
				+ dawdlerBootstrapMains + '}';
	}
}
