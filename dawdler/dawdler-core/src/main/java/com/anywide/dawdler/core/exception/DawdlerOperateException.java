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
package com.anywide.dawdler.core.exception;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerOperateException.java
 * @Description 操作异常
 * @date 2015年06月12日
 * @email suxuan696@gmail.com
 */
public class DawdlerOperateException extends RuntimeException {
    private static final long serialVersionUID = -3550553336288122682L;

    public DawdlerOperateException(String msg) {
        super(msg);
    }

    public DawdlerOperateException(Exception e) {
        super(e);
    }

    public DawdlerOperateException(Throwable e) {
        super(e);
    }

    @Override
    public void printStackTrace() {
        System.err.println(getMessage());
    }
}
