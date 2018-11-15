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
package com.anywide.dawdler.clientplug.web.upload;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @Title:  UploadFile.java   
 * @Description:    上传文件时包装的类   
 * @author: jackson.song    
 * @date:   2007年04月17日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class UploadFile {
	private static Logger logger = LoggerFactory.getLogger(UploadFile.class);
	private FileItem file;
	public UploadFile(FileItem file) {
		this.file=file;
	}
	public InputStream getInputStream() throws IOException{
		return file.getInputStream();
	}
	public byte[] getBytes(){
		return file.get();
	}
	public String getFileName(){
		try {
			return URLDecoder.decode(file.getName(),"utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("",e);
			return file.getName();
		}
	}
	public long getSize(){
		return file.getSize();
	}
	public void delete(){
		if(file!=null)
			file.delete();
	}
}

