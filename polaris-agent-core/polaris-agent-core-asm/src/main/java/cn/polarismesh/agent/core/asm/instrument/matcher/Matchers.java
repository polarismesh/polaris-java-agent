/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
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

package cn.polarismesh.agent.core.asm.instrument.matcher;

import java.util.List;

public final class Matchers {

    private Matchers() {
    }

    public static Matcher newClassNameMatcher(String classInternalName) {
        return new DefaultClassNameMatcher(classInternalName);
    }

    public static Matcher newMultiClassNameMatcher(List<String> classNameList) {
        return new DefaultMultiClassNameMatcher(classNameList);
    }
//
//    public static Matcher newPackageBasedMatcher(String basePackageName) {
//        return new DefaultPackageBasedMatcher(basePackageName);
//    }
//
//    public static Matcher newPackageBasedMatcher(String basePackageName, MatcherOperand additional) {
//        return new DefaultPackageBasedMatcher(basePackageName, additional);
//    }
//
//    public static Matcher newPackageBasedMatcher(List<String> basePackageNames) {
//        return new DefaultMultiPackageBasedMatcher(basePackageNames);
//    }
//
//    public static Matcher newPackageBasedMatcher(List<String> basePackageNames, MatcherOperand additional) {
//        return new DefaultMultiPackageBasedMatcher(basePackageNames, additional);
//    }
//
//    public static Matcher newClassBasedMatcher(String baseClassName) {
//        return new DefaultClassBasedMatcher(baseClassName);
//    }
//
//    public static Matcher newClassBasedMatcher(String baseClassName, MatcherOperand additional) {
//        return new DefaultClassBasedMatcher(baseClassName, additional);
//    }
//
//    public static Matcher newMultiClassBasedMatcher(List<String> baseClassNames) {
//        return new DefaultMultiClassBasedMatcher(baseClassNames);
//    }
//
//    public static Matcher newMultiClassBasedMatcher(List<String> baseClassNames, MatcherOperand additional) {
//        return new DefaultMultiClassBasedMatcher(baseClassNames, additional);
//    }
}