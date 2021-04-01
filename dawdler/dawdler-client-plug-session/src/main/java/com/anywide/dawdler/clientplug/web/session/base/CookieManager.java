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
package com.anywide.dawdler.clientplug.web.session.base;

import com.anywide.dawdler.util.JVMTimeProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author jackson.song
 * @version V1.0
 * @Title CookieManager.java
 * @Description cookie操作  直接copy tomcat中的代码
 * @date 2016年6月16日
 * @email suxuan696@gmail.com
 */
public class CookieManager {
    private static final String OLD_COOKIE_PATTERN =
            "EEE, dd-MMM-yyyy HH:mm:ss z";
    private static final ThreadLocal<DateFormat> OLD_COOKIE_FORMAT =
            ThreadLocal.withInitial(() -> {
                DateFormat df =
                        new SimpleDateFormat(OLD_COOKIE_PATTERN, Locale.US);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df;
            });
    private static final String ancientDate;

    static {
        ancientDate = OLD_COOKIE_FORMAT.get().format(new Date(10000));
    }

    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String value, String domain, String path, int maxAge, String comment, int version, boolean isSecure, boolean isHttpOnly) {
		/*Cookie cookie = new Cookie(cookiename, value);
		if(comment!=null)cookie.setComment(comment);
		if(domain!=null)cookie.setDomain(domain);
		cookie.setMaxAge(maxage);
		if(path!=null)cookie.setPath(path);
		if(version>0)cookie.setVersion(version);
		if(secure)cookie.setSecure(secure);
		response.addCookie(cookie);*/
        StringBuffer sb = new StringBuffer(64);
        appendCookieValue(sb, version, cookieName, value, path, domain, comment, maxAge, isSecure, isHttpOnly);
        response.addHeader("Set-Cookie", sb.toString());
    }

    // Note: Servlet Spec =< 3.0 only refers to Netscape and RFC2109,
    // not RFC2965

    // Version 2 (RFC2965) attributes that would need to be added to support
    // v2 cookies
    // CommentURL
    // Discard - implied by maxAge <0
    // Port


    // -------------------- utils --------------------


    // -------------------- Cookie parsing tools

    private static void appendCookieValue(StringBuffer headerBuf,
                                          int version,
                                          String name,
                                          String value,
                                          String path,
                                          String domain,
                                          String comment,
                                          int maxAge,
                                          boolean isSecure,
                                          boolean isHttpOnly) {
        StringBuffer buf = new StringBuffer();
        buf.append(name);
        buf.append("=");
        int newVersion = version;
        if (newVersion == 0 &&
                (!CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 &&
                        CookieSupport.isHttpToken(value) ||
                        CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 &&
                                CookieSupport.isV0Token(value))) {
            newVersion = 1;
        }

        if (newVersion == 0 && comment != null) {
            newVersion = 1;
        }

        if (newVersion == 0 &&
                (!CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 &&
                        CookieSupport.isHttpToken(path) ||
                        CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 &&
                                CookieSupport.isV0Token(path))) {
            // HTTP token in path - need to use v1
            newVersion = 1;
        }

        if (newVersion == 0 &&
                (!CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 &&
                        CookieSupport.isHttpToken(domain) ||
                        CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 &&
                                CookieSupport.isV0Token(domain))) {
            newVersion = 1;
        }
        maybeQuote(buf, value);
        if (newVersion == 1) {
            buf.append("; Version=1");
            if (comment != null) {
                buf.append("; Comment=");
                maybeQuote(buf, comment);
            }
        }
        if (domain != null) {
            buf.append("; Domain=");
            maybeQuote(buf, domain);
        }
        if (maxAge >= 0) {
            if (newVersion > 0) {
                buf.append("; Max-Age=");
                buf.append(maxAge);
            }
            if (newVersion == 0 || CookieSupport.ALWAYS_ADD_EXPIRES) {
                buf.append("; Expires=");
                if (maxAge == 0)
                    buf.append(ancientDate);
                else
                    OLD_COOKIE_FORMAT.get().format(
                            new Date(JVMTimeProvider.currentTimeMillis() +
                                    maxAge * 1000L),
                            buf, new FieldPosition(0));
            }
        }
        if (path != null) {
            buf.append("; Path=");
            maybeQuote(buf, path);
        }
        if (isSecure) {
            buf.append("; Secure");
        }
        if (isHttpOnly) {
            buf.append("; HttpOnly");
        }
        headerBuf.append(buf);
    }

    /**
     * Quotes values if required.
     *
     * @param buf
     * @param value
     */
    private static void maybeQuote(StringBuffer buf, String value) {
        if (value == null || value.length() == 0) {
            buf.append("\"\"");
        } else if (CookieSupport.alreadyQuoted(value)) {
            buf.append('"');
            buf.append(escapeDoubleQuotes(value, 1, value.length() - 1));
            buf.append('"');
        } else if (CookieSupport.isHttpToken(value) &&
                !CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 ||
                CookieSupport.isV0Token(value) &&
                        CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0) {
            buf.append('"');
            buf.append(escapeDoubleQuotes(value, 0, value.length()));
            buf.append('"');
        } else {
            buf.append(value);
        }
    }


    /**
     * Escapes any double quotes in the given string.
     *
     * @param s          the input string
     * @param beginIndex start index inclusive
     * @param endIndex   exclusive
     * @return The (possibly) escaped string
     */
    private static String escapeDoubleQuotes(String s, int beginIndex, int endIndex) {

        if (s == null || s.length() == 0 || s.indexOf('"') == -1) {
            return s;
        }

        StringBuffer b = new StringBuffer();
        for (int i = beginIndex; i < endIndex; i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                b.append(c);
                //ignore the character after an escape, just append it
                if (++i >= endIndex) throw new IllegalArgumentException("Invalid escape character in cookie value.");
                b.append(s.charAt(i));
            } else if (c == '"')
                b.append('\\').append('"');
            else
                b.append(c);
        }

        return b.toString();
    }
}
