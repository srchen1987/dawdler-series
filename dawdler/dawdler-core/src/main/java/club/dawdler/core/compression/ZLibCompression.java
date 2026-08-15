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
package club.dawdler.core.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author jackson.song
 * @version V1.0
 * Copy过来的Zlib实现
 */
public class ZLibCompression implements CompressionAlgorithm {

	private static final int BUFFER_SIZE = 8192;

	public byte[] compress(byte[] buffer) throws IOException {
		Deflater compressor = new Deflater();
		try {
			compressor.setInput(buffer);
			compressor.finish();
			ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
			byte[] buf = new byte[BUFFER_SIZE];
			while (!compressor.finished()) {
				int i = compressor.deflate(buf);
				bos.write(buf, 0, i);
			}
			return bos.toByteArray();
		} catch (Exception e) {
			throw new IOException("ZLib compress failed.", e);
		} finally {
			compressor.end();
		}
	}

	public byte[] decompress(byte[] buffer) throws IOException {
		Inflater decompressor = new Inflater();
		try {
			decompressor.setInput(buffer);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
			byte[] buf = new byte[BUFFER_SIZE];
			while (!decompressor.finished()) {
				int i = decompressor.inflate(buf);
				if (i == 0) {
					if (decompressor.needsInput() || decompressor.needsDictionary()) {
						throw new DataFormatException("ZLib decompress data is incomplete or corrupted.");
					}
					break;
				}
				bos.write(buf, 0, i);
			}
			return bos.toByteArray();
		} catch (DataFormatException e) {
			throw new IOException("ZLib decompress failed.", e);
		} finally {
			decompressor.end();
		}
	}
}
