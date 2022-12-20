/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.plugin.nacos.utils;



public class StringUtils {
    
    public static final String DOT = ".";
    
    private static final int INDEX_NOT_FOUND = -1;
    
    public static final String COMMA = ",";
    
    public static final String EMPTY = "";
    
    public static final String LF = "\n";
    
    private static final String[] EMPTY_STRING_ARRAY = {};
    
    private static final String TOP_PATH = "..";
    
    private static final String FOLDER_SEPARATOR = "/";
    
    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";
    

    /**
     * <p>Checks if a string is  empty (""), null and  whitespace only.</p>
     *
     * @param cs the string to check
     * @return {@code true} if the string is empty and null and whitespace
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * <p>Checks if a string is not empty (""), not null and not whitespace only.</p>
     *
     * @param str the string to check, may be null
     * @return {@code true} if the string is not empty and not null and not whitespace
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * <p>Checks if a str is not empty ("") or not null.</p>
     *
     * @param str the str to check, may be null
     * @return {@code true} if the str is not empty or not null
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * <p>Checks if a str is empty ("") or null.</p>
     *
     * @param str the str to check, may be null
     * @return {@code true} if the str is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    

}
