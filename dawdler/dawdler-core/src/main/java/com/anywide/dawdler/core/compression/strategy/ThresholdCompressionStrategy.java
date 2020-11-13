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
package com.anywide.dawdler.core.compression.strategy;
import java.io.IOException;

import com.anywide.dawdler.core.compression.CompressionAlgorithm;
import com.anywide.dawdler.core.compression.ZLibCompression;
public class ThresholdCompressionStrategy implements CompressionStrategy {
	private static ThresholdCompressionStrategy thresholdCompressionStrategy = new ThresholdCompressionStrategy();
	public static ThresholdCompressionStrategy staticSingle(){
		return thresholdCompressionStrategy;
	}
	int threshold;
	CompressionAlgorithm compressionAlgorithm;

	public ThresholdCompressionStrategy() {
		this(10240*4);
	}

	public ThresholdCompressionStrategy(int threshold) {
		this(threshold, new ZLibCompression());
	}

	public ThresholdCompressionStrategy(int threshold, CompressionAlgorithm compressionAlgorithm) {
		this.threshold = threshold;
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public CompressionWrapper compress(byte[] buffer) throws IOException {
		CompressionWrapper result = new CompressionWrapper(false, buffer);
		if (buffer.length > threshold) {
			byte[] bytes = compressionAlgorithm.compress(buffer);
			if (bytes.length < buffer.length) {
				result.setBuffer(bytes);
				result.setCompressed(true);
			}
		}
		return result;
	}

	public byte[] decompress(byte[] buffer) throws IOException {
		return compressionAlgorithm.decompress(buffer);
	}

	public void setCompressionAlgorithm(CompressionAlgorithm compressionAlgorithm) {
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public CompressionAlgorithm getCompressionAlgorithm() {
		return compressionAlgorithm;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
}
