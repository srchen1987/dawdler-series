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
package com.anywide.dawdler.clientplug.web.upload;

import com.anywide.dawdler.clientplug.web.handler.ViewForward;
import com.anywide.dawdler.clientplug.web.plugs.DisplaySwitcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jackson.song
 * @version V1.0
 * @Title FileUploadExceptionHolder.java
 * @Description 文件上传异常处理持有者，可以注册，提供了json默认的处理方式
 * @date 2007年04月23日
 * @email suxuan696@gmail.com
 */
public class FileUploadExceptionHolder {
    public static final short JSON = 1;
    private static final Logger logger = LoggerFactory.getLogger(FileUploadExceptionHolder.class);
    private static final ConcurrentHashMap<Short, FileUploadExceptionHandler> handles = new ConcurrentHashMap<>();

    static {
        handles.put(JSON, new JSONFileUploadExceptionHandler());
    }

    public static void regist(short id, FileUploadExceptionHandler handler) {
        FileUploadExceptionHandler handlerPre = handles.putIfAbsent(id, handler);
        if (handlerPre != null) {
            logger.warn("FileUploadExceptionHandler id : " + id + "\talready exists!");
        }
    }

    public static FileUploadExceptionHandler getFileUploadExceptionHandler(short id) {
        return handles.get(id);
    }

    public static FileUploadExceptionHandler getJsonFileUploadExceptionHandler() {
        return getFileUploadExceptionHandler(JSON);
    }

    public static class JSONFileUploadExceptionHandler implements FileUploadExceptionHandler {
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, ViewForward viewForward,
                           Exception ex) {
            viewForward.putData("success", false);
            viewForward.putData("msg", ex.getMessage());
            DisplaySwitcher.switchDisplay(viewForward);
        }

    }
}
