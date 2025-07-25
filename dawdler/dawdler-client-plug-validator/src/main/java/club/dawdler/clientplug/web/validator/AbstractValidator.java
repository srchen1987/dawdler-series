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
package club.dawdler.clientplug.web.validator;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 验证器的抽象类
 */
public abstract class AbstractValidator {
	private static final Logger logger = LoggerFactory.getLogger(AbstractValidator.class);
	private final Pattern pattern;

	public AbstractValidator(String patternKey) {
		this.pattern = RegexRules.getPatternRule(patternKey);
		validatePatternIsNull(pattern, patternKey);
	}

	public boolean validate(String value) {
		if (value == null || value.trim().equals("")) {
			return true;
		}
		if (pattern == null) {
			return true;
		}
		return pattern.matcher(value).find();
	}

	private void validatePatternIsNull(Pattern pattern, String patternKey) {
		if (pattern == null) {
			logger.error(patternKey + "\tis not register,plesase register it in " + RegexRules.class.getName()
					+ " pattern can't null!");
		}
	}
}
