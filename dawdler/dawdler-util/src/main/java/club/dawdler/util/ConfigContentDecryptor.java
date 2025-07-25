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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 配置内容解密器
 */
public class ConfigContentDecryptor {
	private static final Logger logger = LoggerFactory.getLogger(ConfigContentDecryptor.class);
	private static Pattern pattern = Pattern.compile("ENC\\((.+)\\)");
	public static final String DAWDLER_ENCRYPT_FILE = "DAWDLER_ENCRYPT_FILE";
	private static AesSecurityPlus aesSecurityPlus = null;
	private static final String[] SPECIFIC_SYMBOL = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}",
			"|" };

	static {
		try {
			String encryptFile = getDawdlerEncryptFilePath();
			if (encryptFile != null) {
				String key;
				try (InputStream input = new FileInputStream(encryptFile)) {
					byte[] data = IOUtil.toByteArray(input);
					key = new String(data);
				}
				aesSecurityPlus = AesSecurityPlus.getInstance(key);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public static String decryptAndReplaceTag(String content) throws Exception {
		checkAesSecurityPlus(aesSecurityPlus);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			boolean result = false;
			StringBuffer sb = new StringBuffer();
			do {
				String word = null;
				try {
					word = aesSecurityPlus.decrypt(matcher.group(1));
				} catch (Exception e) {
					logger.error("{}", matcher.group(1), e);
					continue;
				}
				matcher.appendReplacement(sb, escapeExprSpecialWord(word));
				result = matcher.find();
			} while (result);
			matcher.appendTail(sb);
			return sb.toString();
		}
		return content;
	}

	public static String escapeExprSpecialWord(String word) {
		for (String key : SPECIFIC_SYMBOL) {
			if (word.contains(key)) {
				word = word.replace(key, "\\" + key);
			}
		}
		return word;
	}

	public static String encrypt(String content) throws Exception {
		checkAesSecurityPlus(aesSecurityPlus);
		return aesSecurityPlus.encrypt(content);
	}

	public static String decrypt(String content) throws Exception {
		return aesSecurityPlus.decrypt(content);
	}


	private static void checkAesSecurityPlus(AesSecurityPlus aesSecurityPlus) {
		if (aesSecurityPlus == null) {
			String encryptFile = getDawdlerEncryptFilePath();
			if (encryptFile == null) {
				logger.error("not found DAWDLER_ENCRYPT_FILE in environment!");
			}
			throw new NullPointerException(
					"aesSecurityPlus is null because DecryptConfigContent initialization failed!");
		}

	}

	public static boolean useDecrypt() {
		return getDawdlerEncryptFilePath() != null;
	}

	public static String getDawdlerEncryptFilePath() {
		return System.getenv(DAWDLER_ENCRYPT_FILE);
	}

}
