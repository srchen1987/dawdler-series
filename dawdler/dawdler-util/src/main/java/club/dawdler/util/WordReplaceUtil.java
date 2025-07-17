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
package club.dawdler.util;

/**
 * @author jackson.song
 * @version V1.0
 * 替换整词的工具类
 */
public class WordReplaceUtil {
    public static String replaceSpecialWord(String text, String target, String replacement) {
        if (text == null || target == null || replacement == null) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int textLen = text.length();
        int targetLen = target.length();
        int start = 0;

        while (start < textLen) {
            int index = text.indexOf(target, start);
            if (index == -1) {
                result.append(text.substring(start));
                break;
            }

            // 检查前后字符
            boolean isWordStart = isWordBoundary(text, index, true);
            boolean isWordEnd = isWordBoundary(text, index + targetLen, false);

            if (isWordStart && isWordEnd) {
                result.append(text.substring(start, index)).append(replacement);
            } else {
                result.append(text.substring(start, index + targetLen));
            }
            start = index + targetLen;
        }

        return result.toString();
    }

    /**
     * 检查是否为词边界
     */
    private static boolean isWordBoundary(String text, int index, boolean isStart) {
        if (isStart) {
            return index == 0 || !isWordChar(text.charAt(index - 1));
        } else {
            return index >= text.length() || !isWordChar(text.charAt(index));
        }
    }

    /**
     * 判断字符是否为词字符（包括中文）
     */
    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) ||
                (c >= '\u4e00' && c <= '\u9fa5') || // 中文字符范围
                c == '_';
    }
}
