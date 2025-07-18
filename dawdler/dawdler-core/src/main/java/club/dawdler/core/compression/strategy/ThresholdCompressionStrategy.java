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
package club.dawdler.core.compression.strategy;

import java.io.IOException;

import club.dawdler.core.compression.CompressionAlgorithm;
import club.dawdler.core.compression.ZLibCompression;

/**
 * @author jackson.song
 * @version V1.0
 * 基于数据包大小的压缩策略，返回压缩包装类
 */
public class ThresholdCompressionStrategy implements CompressionStrategy {

	private static final ThresholdCompressionStrategy thresholdCompressionStrategy = new ThresholdCompressionStrategy();
	CompressionAlgorithm compressionAlgorithm;
	private int threshold;

	public ThresholdCompressionStrategy() {
		this(10240 * 4);
	}

	public ThresholdCompressionStrategy(int threshold) {
		this(threshold, new ZLibCompression());
	}

	public ThresholdCompressionStrategy(int threshold, CompressionAlgorithm compressionAlgorithm) {
		this.threshold = threshold;
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public static ThresholdCompressionStrategy staticSingle() {
		return thresholdCompressionStrategy;
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

	public CompressionAlgorithm getCompressionAlgorithm() {
		return compressionAlgorithm;
	}

	public void setCompressionAlgorithm(CompressionAlgorithm compressionAlgorithm) {
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
}
