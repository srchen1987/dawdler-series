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
package com.anywide.dawdler.core.compression;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
/**
 * 
 * @Title:  ZLibCompression.java
 * @Description:    Copy过来的Zlib实现   
 * @author: jackson.song    
 * @date:   2015年07月16日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class ZLibCompression implements CompressionAlgorithm {

	public byte[] compress(byte[] buffer) throws IOException {
		byte[] output = new byte[0];
		Deflater compresser = new Deflater();
		compresser.reset();
		compresser.setInput(buffer);
		compresser.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
		try {
			byte[] buf = new byte[1024];
			while (!compresser.finished()) {
				int i = compresser.deflate(buf);
				bos.write(buf, 0, i);
			}
			output = bos.toByteArray();
		} catch (Exception e) {
			output = buffer;
		}
		compresser.end();
		return output;
	}

	public byte[] decompress(byte[] buffer) throws IOException {
		byte[] output = new byte[0];
		Inflater decompresser = new Inflater();
		decompresser.reset();
		decompresser.setInput(buffer);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
		try {
			byte[] buf = new byte[1024];
			while (!decompresser.finished()) {
				int i = decompresser.inflate(buf);
				bos.write(buf, 0, i);
			}
			output = bos.toByteArray();
		} catch (Exception e) {
			output = buffer;
		}
		decompresser.end();
		return output;
	}
}
