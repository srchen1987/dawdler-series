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
package club.dawdler.clientplug.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jackson.song
 * @version V1.0
 * 时间格式化注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface DateTimeFormat {

	ISO iso() default ISO.NONE;

	String pattern() default "";

	/**
	 * Common ISO date time format patterns.
	 */
	enum ISO {

		/**
		 * The most common ISO Date Format {@code yyyy-MM-dd},
		 * e.g. "2000-10-31".
		 */
		DATE,

		/**
		 * The most common ISO Time Format {@code HH:mm:ss.SSSZ},
		 * e.g. "01:30:00.000-05:00".
		 */
		TIME,

		/**
		 * The most common ISO DateTime Format {@code yyyy-MM-dd'T'HH:mm:ss.SSSZ},
		 * e.g. "2000-10-31T01:30:00.000-05:00".
		 * <p>
		 * This is the default if no annotation value is specified.
		 */
		DATE_TIME,

		/**
		 * Indicates that no ISO-based format pattern should be applied.
		 */
		NONE
	}
	public final static String ISO_8601_DATE_PATTERN = "yyyy-MM-dd";
	public final static String ISO_8601_TIME_PATTERN = "HH:mm:ss.SSSZ";
	public final static String ISO_8601_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

}
