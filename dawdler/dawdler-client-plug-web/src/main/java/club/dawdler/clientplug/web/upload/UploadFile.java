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
package club.dawdler.clientplug.web.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.fileupload.FileItem;

/**
 * @author jackson.song
 * @version V1.0
 * 上传文件时包装的类
 */
public class UploadFile {
	private final FileItem file;

	public UploadFile(FileItem file) {
		this.file = file;
	}

	public InputStream getInputStream() throws IOException {
		return file.getInputStream();
	}

	public byte[] getBytes() {
		return file.get();
	}

	public String getFileName() {
		try {
			return URLDecoder.decode(file.getName(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			return file.getName();
		}
	}

	public long getSize() {
		return file.getSize();
	}

	public void delete() {
		if (file != null) {
			file.delete();
		}
	}
}
