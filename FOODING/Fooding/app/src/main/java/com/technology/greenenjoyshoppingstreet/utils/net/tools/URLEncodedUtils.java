package com.technology.greenenjoyshoppingstreet.utils.net.tools;
/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */


import com.technology.greenenjoyshoppingstreet.utils.net.KeyValuePair;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.List;

/**
 * A collection of utilities for encoding URLs.
 *
 * @version V1.0
 *  2017.05.11
 * @since 4.0
 */
public class URLEncodedUtils {

    /** Content type. */
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    /** Parameter separator. */
    private static final String PARAMETER_SEPARATOR = "&";
    /** Name value separator. */
    private static final String NAME_VALUE_SEPARATOR = "=";


    /** Delim. */
    private static final char[] DELIM = new char[]{'&'};


    /**
     * Returns a String that is suitable for use as an <code>application/x-www-form-urlencoded</code>
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters The parameters to include.
     * @param encoding   The encoding to use.
     * @return the string
     */
    public static String format(
            final List<? extends KeyValuePair> parameters,
            final String encoding) {
        final StringBuilder result = new StringBuilder();
        for (final KeyValuePair parameter : parameters) {
            final String encodedName = encodeFormFields(parameter.getKey(), Charset.forName
                    (encoding));
            final String encodedValue = encodeFormFields(parameter.getValue(), Charset.forName
                    (encoding));
            if (result.length() > 0) {
                result.append(PARAMETER_SEPARATOR);
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append(NAME_VALUE_SEPARATOR);
                result.append(encodedValue);
            }
        }
        return result.toString();
    }


    /**
     * Unreserved characters, i.e. alphanumeric, plus: {@code _ - ! . ~ ' ( ) *}
     * <p>
     * This list is the same as the {@code unreserved} list in
     * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
     */
    private static final BitSet UNRESERVED = new BitSet(256);
    /**
     * Punctuation characters: , ; : $ & + =
     * <p>
     * These are the additional characters allowed by userinfo.
     */
    private static final BitSet PUNCT = new BitSet(256);
    /**
     * Characters which are safe to use in userinfo, i.e. {@link #UNRESERVED} plus {@link #PUNCT}uation
     */
    private static final BitSet USERINFO = new BitSet(256);
    /**
     * Characters which are safe to use in a path, i.e. {@link #UNRESERVED} plus {@link #PUNCT}uation plus / @
     */
    private static final BitSet PATHSAFE = new BitSet(256);
    /**
     * Characters which are safe to use in a fragment, i.e. {@link #RESERVED} plus {@link #UNRESERVED}
     */
    private static final BitSet FRAGMENT = new BitSet(256);

    /**
     * Reserved characters, i.e. {@code ;/?:@&=+$,[]}
     * <p>
     * This list is the same as the {@code reserved} list in
     * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
     * as augmented by
     * <a href="http://www.ietf.org/rfc/rfc2732.txt">RFC 2732</a>
     */
    private static final BitSet RESERVED = new BitSet(256);


    /**
     * Safe characters for x-www-form-urlencoded data, as per java.net.URLEncoder and browser behaviour,
     * i.e. alphanumeric plus {@code "-", "_", ".", "*"}
     */
    private static final BitSet URLENCODER = new BitSet(256);

    static {
        // unreserved chars
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            UNRESERVED.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            UNRESERVED.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            UNRESERVED.set(i);
        }
        UNRESERVED.set('_'); // these are the charactes of the "mark" list
        UNRESERVED.set('-');
        UNRESERVED.set('.');
        UNRESERVED.set('*');
        URLENCODER.or(UNRESERVED); // skip remaining unreserved characters
        UNRESERVED.set('!');
        UNRESERVED.set('~');
        UNRESERVED.set('\'');
        UNRESERVED.set('(');
        UNRESERVED.set(')');
        // punct chars
        PUNCT.set(',');
        PUNCT.set(';');
        PUNCT.set(':');
        PUNCT.set('$');
        PUNCT.set('&');
        PUNCT.set('+');
        PUNCT.set('=');
        // Safe for userinfo
        USERINFO.or(UNRESERVED);
        USERINFO.or(PUNCT);

        // URL path safe
        PATHSAFE.or(UNRESERVED);
        PATHSAFE.set('/'); // segment separator
        PATHSAFE.set(';'); // param separator
        PATHSAFE.set(':'); // rest as per list in 2396, i.e. : @ & = + $ ,
        PATHSAFE.set('@');
        PATHSAFE.set('&');
        PATHSAFE.set('=');
        PATHSAFE.set('+');
        PATHSAFE.set('$');
        PATHSAFE.set(',');

        RESERVED.set(';');
        RESERVED.set('/');
        RESERVED.set('?');
        RESERVED.set(':');
        RESERVED.set('@');
        RESERVED.set('&');
        RESERVED.set('=');
        RESERVED.set('+');
        RESERVED.set('$');
        RESERVED.set(',');
        RESERVED.set('['); // added by RFC 2732
        RESERVED.set(']'); // added by RFC 2732

        FRAGMENT.or(RESERVED);
        FRAGMENT.or(UNRESERVED);
    }

    /** Radix. */
    private static final int RADIX = 16;

    /**
     * Emcode/escape a portion of a URL, to use with the query part ensure {@code plusAsBlank} is true.
     *
     * @param content     the portion to decode
     * @param charset     the charset to use
     * @param safechars   the safechars
     * @param blankAsPlus if {@code true}, then convert space to '+' (e.g. for www-url-form-encoded content), otherwise leave as is.
     * @return string
     */
    private static String urlencode(
            final String content,
            final Charset charset,
            final BitSet safechars,
            final boolean blankAsPlus) {
        if (content == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xff;
            if (safechars.get(b)) {
                buf.append((char) b);
            } else if (blankAsPlus && b == ' ') {
                buf.append('+');
            } else {
                buf.append("%");
                char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                buf.append(hex1);
                buf.append(hex2);
            }
        }
        return buf.toString();
    }


    /**
     * Encode/escape www-url-form-encoded content.
     * <p>
     * Uses the {@link #URLENCODER} set of characters, rather than
     * releases, URLEncoder.encode() and most browsers.
     *
     * @param content the content to encode, will convert space to '+'
     * @param charset the charset to use
     * @return string
     */
    private static String encodeFormFields(final String content, final Charset charset) {
        if (content == null) {
            return null;
        }
        return urlencode(content, charset != null ? charset : Charset.forName("UTF-8"), URLENCODER,
                true);
    }


}