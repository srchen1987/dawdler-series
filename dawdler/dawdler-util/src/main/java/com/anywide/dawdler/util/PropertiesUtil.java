package com.anywide.dawdler.util;

import java.io.FileInputStream;
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

	public static Properties loadActiveProfileProperties(String fileName) throws IOException {
		String activeProfile = System.getProperty("dawdler.profiles.active");
		return loadProperties(fileName + (activeProfile != null ? "-" + activeProfile : ""));
	}

	public static Properties loadActiveProfileIfNotExistUseDefaultProperties(String fileName) throws IOException {
		try {
			return loadActiveProfileProperties(fileName);
		} catch (Exception e) {
			return loadProperties(fileName);
		}
	}
}
