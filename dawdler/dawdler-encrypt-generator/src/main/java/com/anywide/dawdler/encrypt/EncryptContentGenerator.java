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
package com.anywide.dawdler.encrypt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.anywide.dawdler.util.AesSecurityPlus;
import com.anywide.dawdler.util.ConfigContentDecryptor;

/**
 * @author jackson.song
 * @version V1.0
 * @Title EncryptContentGenerator.java
 * @Description 用于加密内容的类(同时生成密钥文件)
 * @date 2022年6月21日
 * @email suxuan696@gmail.com
 */
public class EncryptContentGenerator {
	public static final String DAWDLER_ENCRYP_FILE = "DAWDLER_ENCRYP_FILE";

	public static void main(String[] args) throws Exception {
		String path = ConfigContentDecryptor.getDawdlerEncrypFilePath();
		if (path == null) {
			generate();
			return;
		}
		File file = new File(path);
		if (file.exists()) {
			if (args.length < 1) {
				System.out.println("please type need encrypt content!");
				return;
			}
			String content = args[0];
			System.out.println(content + " -> [" + ConfigContentDecryptor.encrypt(content) + "]");

		} else {
			generate();
		}
	}

	private static void generate() throws Exception {
		AesSecurityPlus ap = AesSecurityPlus.DEFAULT_INSTANCE;
		File file = new File("dawdler.password");
		System.out.println("generated file:[" + file.getAbsolutePath() + "]");
		System.out.println("please set DAWDLER_ENCRYP_FILE=" + file.getAbsolutePath() + " to environment!");
		try (OutputStream out = new FileOutputStream(file)) {
			out.write(ap.encrypt(UUID.randomUUID().toString()).getBytes());
			out.flush();
		}
	}
}
