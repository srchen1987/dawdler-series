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
package club.dawdler.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jackson.song
 * @version V1.0
 * 日期工具类
 */
public class DateUtil {

	private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

	public static String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	public static String DEFAULT_TIME_PATTERN = "HH:mm:ss";

	static {
		String datetime = System.getProperty("dawdler.datetime.format");
		if (datetime != null && !datetime.trim().equals("")) {
			DEFAULT_DATE_TIME_PATTERN = datetime;
		}
		String date = System.getProperty("dawdler.date.format");
		if (date != null && !date.trim().equals("")) {
			DEFAULT_DATE_PATTERN = date;
		}
		String time = System.getProperty("dawdler.time.format");
		if (time != null && !time.trim().equals("")) {
			DEFAULT_TIME_PATTERN = time;
		}
		FORMATTER_CACHE.put(DEFAULT_DATE_TIME_PATTERN, DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN));
		FORMATTER_CACHE.put(DEFAULT_DATE_PATTERN, DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN));
		FORMATTER_CACHE.put(DEFAULT_TIME_PATTERN, DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN));
	}

	public static DateTimeFormatter getFormatter(String pattern) {
		DateTimeFormatter formatter = FORMATTER_CACHE.get(pattern);
		if (formatter != null) {
			return formatter;
		}

		DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern(pattern);

		formatter = FORMATTER_CACHE.putIfAbsent(pattern, newFormatter);

		return formatter == null ? newFormatter : formatter;
	}

	public static int getCacheSize() {
		return FORMATTER_CACHE.size();
	}

	public static void clearCache() {
		FORMATTER_CACHE.clear();
	}

	public static DateTimeFormatter removePattern(String pattern) {
		return FORMATTER_CACHE.remove(pattern);
	}

	public static Date convert(Object value) {
		if (value instanceof Date) {
			return (Date) value;
		} else if (value instanceof LocalDateTime) {
			return Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
		} else if (value instanceof LocalDate) {
			return Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
		} else if (value instanceof LocalTime) {
			return Date.from(((LocalTime) value).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
		} else if (value instanceof ZonedDateTime) {
			return Date.from(((ZonedDateTime) value).toInstant());
		} else if (value instanceof OffsetDateTime) {
			return Date.from(((OffsetDateTime) value).toInstant());
		}
		return null;
	}

	public static boolean isDateType(Class<?> type) {
		return Date.class == type || LocalDate.class == type || LocalDateTime.class == type || LocalTime.class == type
				|| ZonedDateTime.class == type || OffsetDateTime.class == type;
	}

	public static boolean isDateTypeArray(Class<?> type) {
		return type.isArray() && isDateType(type.getComponentType());
	}

	public static Date convertToDate(String value, String pattern) {
		if (value == null || value.trim().equals("")) {
			return null;
		}
		if (pattern == null) {
			pattern = DEFAULT_DATE_TIME_PATTERN;
		}
		return Date.from(
				LocalDateTime.parse(value, getFormatter(pattern)).atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDateTime convertToLocalDateTime(String value, String pattern) {
		if (value == null || value.trim().equals("")) {
			return null;
		}
		if (pattern == null) {
			pattern = DEFAULT_DATE_TIME_PATTERN;
		}
		return LocalDateTime.parse(value, getFormatter(pattern));
	}

	public static LocalDate convertToLocalDate(String value, String pattern) {
		if (value == null || value.trim().equals("")) {
			return null;
		}
		if (pattern == null) {
			pattern = DEFAULT_DATE_PATTERN;
		}
		return LocalDate.parse(value, DateUtil.getFormatter(pattern));
	}

	public static LocalTime convertToLocalTime(String value, String pattern) {
		if (value == null || value.trim().equals("")) {
			return null;
		}
		if (pattern == null) {
			pattern = DEFAULT_TIME_PATTERN;
		}
		return LocalTime.parse(value, DateUtil.getFormatter(pattern));
	}

	public static ZonedDateTime convertToZonedDateTime(String value, String pattern) {
		if (value == null || value.trim().equals("")) {
			return null;
		}
		if (pattern == null) {
			pattern = DEFAULT_DATE_TIME_PATTERN;
		}
		return ZonedDateTime.parse(value, DateUtil.getFormatter(pattern));
	}

	public static OffsetDateTime convertToOffsetDateTime(String value, String pattern) {
		if (value == null || value.trim().equals("")) {
			return null;
		}
		if (pattern == null) {
			pattern = DEFAULT_DATE_TIME_PATTERN;
		}
		return OffsetDateTime.parse(value, DateUtil.getFormatter(pattern));
	}

	public static Object convertToDateArray(String[] values, String pattern, Class<?> type) {
		if (values == null || values.length == 0) {
			return null;
		}
		Object[] result = null;
		if (type == Date.class) {
			result = new Date[values.length];
			for (int i = 0; i < values.length; i++) {
				result[i] = convertToDate(values[i], pattern);
			}
		} else if (type == LocalDateTime.class) {
			result = new LocalDateTime[values.length];
			for (int i = 0; i < values.length; i++) {
				result[i] = convertToLocalDateTime(values[i], pattern);
			}
		} else if (type == LocalDate.class) {
			result = new LocalDate[values.length];
			for (int i = 0; i < values.length; i++) {
				result[i] = convertToLocalDate(values[i], pattern);
			}
		} else if (type == LocalTime.class) {
			result = new LocalTime[values.length];
			for (int i = 0; i < values.length; i++) {
				result[i] = convertToLocalTime(values[i], pattern);
			}
		} else if (type == ZonedDateTime.class) {
			result = new ZonedDateTime[values.length];
			for (int i = 0; i < values.length; i++) {
				result[i] = convertToZonedDateTime(values[i], pattern);
			}
		} else if (type == OffsetDateTime.class) {
			result = new OffsetDateTime[values.length];
			for (int i = 0; i < values.length; i++) {
				result[i] = convertToOffsetDateTime(values[i], pattern);
			}
		}
		return result;
	}

	public static Object convertToDate(String value, String pattern, Class<?> type) {
		if (value == null || value.trim().equals("")) {
			return null;
		}
		Object result = null;
		if (type == Date.class) {
			result = convertToDate(value, pattern);
		} else if (type == LocalDateTime.class) {
			result = convertToLocalDateTime(value, pattern);
		} else if (type == LocalDate.class) {
			result = convertToLocalDate(value, pattern);
		} else if (type == LocalTime.class) {
			result = convertToLocalTime(value, pattern);
		} else if (type == ZonedDateTime.class) {
			result = convertToZonedDateTime(value, pattern);
		} else if (type == OffsetDateTime.class) {
			result = convertToOffsetDateTime(value, pattern);
		}
		return result;
	}

}
