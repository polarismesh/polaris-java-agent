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

package cn.polarismesh.agent.core.common.utils;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Objects;

public final class CodeSourceUtils {

    private CodeSourceUtils() {
    }

    public static URL getCodeLocation(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");

        final ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        return getCodeLocation(protectionDomain);
    }


    public static URL getCodeLocation(ProtectionDomain protectionDomain) {
        if (protectionDomain == null) {
            return null;
        }

        final CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            return null;
        }
        return codeSource.getLocation();
    }
}
