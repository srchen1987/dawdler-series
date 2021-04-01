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
package com.anywide.dawdler.clientplug.web.validator.operators;

import java.util.List;
import java.util.regex.Matcher;

/**
 * @author jackson.song
 * @version V1.0
 * @Title MaxSelectRuleOperator.java
 * @Description 选项大于判断
 * @date 2007年07月22日
 * @email suxuan696@gmail.com
 */
public class MaxSelectRuleOperator extends RegexRuleOperator {
    public static final String RULEKEY = "^maxselect:([1-9]{1}\\d*$)";

    public MaxSelectRuleOperator() {
        super(RULEKEY);
    }

    @Override
    public String validate(Object value, Matcher matcher) {
        int i = Integer.parseInt(matcher.group(1));
        String error = "不能大于" + i + "项!";
        if (value == null)
            return null;
        if (value instanceof String[]) {
            if (((String[]) value).length > i)
                return error;
        } else if (value instanceof List) {
            if (((List) value).size() > i)
                return error;
        }
        return null;
    }

    @Override
    public String toString() {
        return "最大选择数或最大参数个数或List或数组的长度不能大于指定数字如:maxselect:3!";
    }
}
