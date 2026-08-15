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
package club.dawdler.util.aspect;

import java.security.ProtectionDomain;

import org.aspectj.weaver.loadtime.Aj;

/**
 * @author jackson.song
 * @version V1.0
 * AspectHolder
 */
public class AspectHolder {
	public static final Aj AJ =new Aj();
	public static byte[] preProcess(String className, byte[] classfileBuffer, ClassLoader loader,
			ProtectionDomain protectionDomain) {
		byte[] result = AJ.preProcess(className, classfileBuffer, loader, protectionDomain);
		if (result != null) {
			return result;
		}
		return classfileBuffer;
	}

}
