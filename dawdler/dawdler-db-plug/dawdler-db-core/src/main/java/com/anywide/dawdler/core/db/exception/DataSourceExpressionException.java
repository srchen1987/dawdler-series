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
package com.anywide.dawdler.core.db.exception;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DataSourceExpressionException.java
 * @Description 表达式异常
 * @date 2022年6月19日
 * @email suxuan696@gmail.com
 */
public class DataSourceExpressionException extends Exception {

	private static final long serialVersionUID = 3052983943906141279L;

	public DataSourceExpressionException(String message) {
		super(message);
	}

}
