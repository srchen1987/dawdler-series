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
package club.dawdler.clientplug.web.jwt.algorithm;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;

import club.dawdler.clientplug.web.jwt.exception.JwtException;

/**
 * @author jackson.song
 * @version V1.0
 * 根据算法名称与密钥材料构造{@link Algorithm}.
 */
public final class AlgorithmFactory {

	private static final Set<String> HMAC_ALGORITHMS = Set.of("HS256", "HS384", "HS512");
	private static final Set<String> RSA_ALGORITHMS = Set.of("RS256", "RS384", "RS512");

	private AlgorithmFactory() {
	}

	public static Algorithm create(String algorithm, String secret, String rsaPrivateKey, String rsaPublicKey) {
		if (algorithm == null || algorithm.trim().isEmpty()) {
			throw new JwtException("algorithm can not be empty");
		}
		String name = algorithm.trim();
		if (HMAC_ALGORITHMS.contains(name)) {
			if (secret == null || secret.isEmpty()) {
				throw new JwtException("algorithm " + name + " need secret");
			}
			return new HmacAlgorithm(name, secret.getBytes(StandardCharsets.UTF_8));
		}
		if (RSA_ALGORITHMS.contains(name)) {
			PrivateKey privateKey = (rsaPrivateKey == null || rsaPrivateKey.trim().isEmpty()) ? null
					: RsaAlgorithm.readPkcs8PrivateKey(rsaPrivateKey);
			PublicKey publicKey = (rsaPublicKey == null || rsaPublicKey.trim().isEmpty()) ? null
					: RsaAlgorithm.readX509PublicKey(rsaPublicKey);
			return new RsaAlgorithm(name, privateKey, publicKey);
		}
		throw new JwtException("unsupported algorithm: " + name);
	}

}
