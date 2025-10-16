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
package club.dawdler.clientplug.web.validator.operators;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jackson.song
 * @version V1.0
 * 不能重复验证
 */
public class UniqueItemsRuleOperator extends StringRuleOperator {
	public static final String RULE_KEY = "uniqueItems";
	public static final String EXPLAIN = "不能重复验证!";

	public UniqueItemsRuleOperator() {
		super(RULE_KEY, null, EXPLAIN);

	}

	@Override
	public String validate(Object value) {
		if (value == null) {
			return null;
		}
		boolean flag = true;
		if (value instanceof String[]) {
			String[] values = (String[]) value;
			Set<String> set = new HashSet<>();
			for (String v : values) {
				if (!set.add(v)) {
					flag = false;
					break;
				}
			}
		}
		if (!flag) {
			return "不允许重复!";
		}
		return null;
	}

	@Override
	public String toString() {
		return EXPLAIN;
	}

}
