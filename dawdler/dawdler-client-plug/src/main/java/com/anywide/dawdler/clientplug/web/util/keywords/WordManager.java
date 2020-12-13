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
package com.anywide.dawdler.clientplug.web.util.keywords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.anywide.dawdler.util.DawdlerTool;

/**
 * 
 * @Title: WordManager.java
 * @Description: TODO
 * @author: jackson.song
 * @date: 2008年05月17日
 * @version V1.0
 * @email: suxuan696@gmail.com
 */
public class WordManager {
//	private volatile long lastModified;
	private AtomicLong lastModified = new AtomicLong();
	private AtomicBoolean remark = new AtomicBoolean(false);
	private static WordManager wordManager = new WordManager();
	private Lock lock = new ReentrantLock();
	private Words words = new Words();
	private File kwfile = new File(DawdlerTool.getcurrentPath() + "keywords.txt");

	private WordManager() {
	}

	public static WordManager getInstance() {
		if (wordManager.isUpdate()) {
			wordManager.lock.lock();
			if (wordManager.isUpdate()) {
				try {
					wordManager.loadFile();
				} catch (IOException e) {
				}
			}
			wordManager.lock.unlock();
		}
		return wordManager;
	}

	public String getKeyWord(String context) {
		return words.getFindedFirstWord(context);
	}

	private boolean isUpdate() {
		return kwfile.lastModified() != lastModified.get();
	}

	private void loadFile() throws IOException {
		this.lastModified.compareAndSet(lastModified.get(), kwfile.lastModified());
		FileInputStream fin = new FileInputStream(kwfile);
		InputStreamReader in = new InputStreamReader(fin);
		BufferedReader br = new BufferedReader(in);
		String keyword = null;
		words.clear();
		while ((keyword = br.readLine()) != null) {
			words.addWord(keyword);
		}
		br.close();
		remark.compareAndSet(false, true);
	}
}
