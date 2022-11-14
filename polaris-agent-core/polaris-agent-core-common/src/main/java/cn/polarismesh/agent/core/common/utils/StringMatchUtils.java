/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.core.common.utils;

import java.util.Objects;

public final class StringMatchUtils {

    private StringMatchUtils() {
    }

    public static int indexOf(String str, char[] chars) {
        Objects.requireNonNull(str, "str");
        Objects.requireNonNull(chars, "chars");

        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (contains(c, chars)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean contains(char c, char[] validChars) {
        Objects.requireNonNull(validChars, "validChars");

        for (char validCh : validChars) {
            if (validCh == c) {
                return true;
            }
        }
        return false;
    }

    public static int endsWithCountMatches(String str, String postfix) {
        Objects.requireNonNull(str, "str");
        if (StringUtils.isEmpty(postfix)) {
            return 0;
        }

        final int postFixLength = postfix.length();

        int count = 0;
        final int lastOffset = str.length() - postFixLength;
        final int length = str.length();
        for (int i = lastOffset; i < length; i -= postFixLength) {
            final boolean found = str.startsWith(postfix, i);
            if (!found) {
                break;
            }
            count++;
        }

        return count;
    }

    public static int startsWithCountMatches(String str, char prefix) {
        if (StringUtils.isEmpty(str)) {
            return 0;
        }

        int count = 0;
        final int length = str.length();
        for (int i = 0; i < length; i++) {
            final char c = str.charAt(i);
            if (c != prefix) {
                break;
            }
            count++;
        }

        return count;
    }

    /**
     * ExperimentalApi
     */
    static void appendAndReplace(String str, int startOffset, char oldChar, char newChar, StringBuilder output) {
        Objects.requireNonNull(str, "str");
        Objects.requireNonNull(output, "output");

        for (int i = startOffset; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c == oldChar) {
                output.append(newChar);
            } else {
                output.append(c);
            }
        }
    }

    /**
     * ExperimentalApi
     */
    public static boolean startWith(String str1, String str2) {
        if (str1 == null) {
            return false;
        }
        return str1.startsWith(str2);
    }

    /**
     * ExperimentalApi
     */

    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return false;
        }
        return str1.equals(str2);
    }

}
