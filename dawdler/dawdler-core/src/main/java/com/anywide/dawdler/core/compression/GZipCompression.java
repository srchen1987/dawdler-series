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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author jackson.song
 * @version V1.0
 * @Title GZipCompression.java
 * @Description copy过来的GZIP实现
 * @date 2015年07月16日
 * @email suxuan696@gmail.com
 */
public class GZipCompression implements CompressionAlgorithm {
    int size = 4096;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] compress(byte[] buffer) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(arrayOutputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);

        try {
            byte[] buf = new byte[size];
            int len = 0;
            while ((len = inputStream.read(buf)) != -1) {
                gzip.write(buf, 0, len);
            }
            gzip.finish();
            return arrayOutputStream.toByteArray();
        } finally {
            gzip.close();
            arrayOutputStream.close();
            inputStream.close();
        }
    }

    public byte[] decompress(byte[] buffer) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        GZIPInputStream gzip = new GZIPInputStream(inputStream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[size];
            int len;
            while ((len = gzip.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        }finally {
            gzip.close();
            out.close();
            inputStream.close();
        }
    }
}
