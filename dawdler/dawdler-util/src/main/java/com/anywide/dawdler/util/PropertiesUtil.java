package com.anywide.dawdler.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	public static int getIfNullReturnDefaultValueInt(String key, int defaultValue, Properties ps) {
		Object value = ps.get(key);
		if (value != null) {
			try {
				return Integer.parseInt(value.toString());
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}

	public static long getIfNullReturnDefaultValueLong(String key, long defaultValue, Properties ps) {
		Object value = ps.get(key);
		if (value != null) {
			try {
				return Long.parseLong(value.toString());
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}

	public static boolean getIfNullReturnDefaultValueBoolean(String key, boolean defaultValue, Properties ps) {
		Object value = ps.get(key);
		if (value != null) {
			try {
				return Boolean.parseBoolean(value.toString());
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}

	public static Properties loadProperties(String fileName) throws IOException {
		String path = DawdlerTool.getcurrentPath() + fileName + ".properties";
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(path);
			Properties ps = new Properties();
			ps.load(inStream);
			return ps;
		} finally {
			if (inStream != null)
				inStream.close();
		}
	}
}
