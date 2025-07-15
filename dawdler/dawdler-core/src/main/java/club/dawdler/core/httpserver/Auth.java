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
package club.dawdler.core.httpserver;

import com.sun.net.httpserver.BasicAuthenticator;

/**
 * @author jackson.song
 * @version V1.0
 * Auth 认证器
 */
public class Auth extends BasicAuthenticator {
	private String username;
	private String password;

	public Auth(String realm, String username, String password) {
		super(realm);
		this.username = username;
		this.password = password;
	}

	@Override
	public boolean checkCredentials(String username, String password) {
		if (this.username.equals(username) && this.password.equals(password)) {
			return true;
		}
		return false;
	}

}
