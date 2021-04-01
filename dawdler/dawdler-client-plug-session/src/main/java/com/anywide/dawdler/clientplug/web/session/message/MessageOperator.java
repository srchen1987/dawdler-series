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
package com.anywide.dawdler.clientplug.web.session.message;

/**
 * @author jackson.song
 * @version V1.0
 * @Title MessageOperator.java
 * @Description 消息操作者
 * @date 2016年6月16日
 * @email suxuan696@gmail.com
 */
public interface MessageOperator {

    void sendMessageToSet(String sessionKey, String attributeName, Object attributeValue);

    void sendMessageToDel(String sessionKey, String attributeName);

    void listenExpireAndDelAndChange();

}
