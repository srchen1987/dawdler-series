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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author jackson.song
 * @version V1.0
 * AES加密解密工具类,替代DES
 */
public class AesSecurityPlus {
	private static ConcurrentHashMap<String, AesSecurityPlus> cachePlug = new ConcurrentHashMap<>();
	public static AesSecurityPlus DEFAULT_INSTANCE;
	private static final String CHARSET = "UTF-8";
	static {
		try {
			DEFAULT_INSTANCE = AesSecurityPlus.getInstance(AesSecurityPlus.class.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private Cipher encipher = null;
	private Cipher decipher = null;

	private SecretKeySpec skeySpec;
	private IvParameterSpec iv;

	private AesSecurityPlus(String password) throws Exception {
		byte[] raw = password.getBytes(CHARSET);
		skeySpec = new SecretKeySpec(raw, "AES");
		encipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		iv = new IvParameterSpec(password.getBytes(CHARSET));
	}

	public static AesSecurityPlus getInstance(String key) throws Exception {
		if (key == null || key.length() < 16) {
			throw new InvalidKeyException("the key size must be >= 16 !");
		}
		if (key.length() > 16) {
			key = key.substring(0, 16);
		}
		AesSecurityPlus sp = cachePlug.get(key);
		if (sp == null) {
			sp = new AesSecurityPlus(key);
			AesSecurityPlus pre = cachePlug.putIfAbsent(key, sp);
			if (pre != null) {
				return pre;
			}
		}
		return sp;
	}

	public String encrypt(String content) throws Exception {
		byte[] encrypted = null;
		synchronized (encipher) {
			encipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			encrypted = encipher.doFinal(content.getBytes(CHARSET));
		}
		String baseString = Base64.getEncoder().encodeToString(encrypted);
		return URLEncoder.encode(baseString, CHARSET);
	}

	public String decrypt(String content) throws Exception {
		content = URLDecoder.decode(content, CHARSET);
		byte[] decrypted = Base64.getDecoder().decode(content.getBytes(CHARSET));
		synchronized (decipher) {
			decipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			decrypted = decipher.doFinal(decrypted);
		}
		return new String(decrypted, CHARSET);
	}

	public byte[] encryptByteArray(byte[] data) throws Exception {
		synchronized (encipher) {
			encipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			return encipher.doFinal(data);
		}
	}

	public byte[] decryptByteArray(byte[] data) throws Exception {
		synchronized (decipher) {
			decipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			return decipher.doFinal(data);
		}
	}

}
