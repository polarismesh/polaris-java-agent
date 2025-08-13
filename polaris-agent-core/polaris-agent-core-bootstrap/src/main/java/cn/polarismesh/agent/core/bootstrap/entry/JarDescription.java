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

package cn.polarismesh.agent.core.bootstrap.entry;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarDescription {

    static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((\\-SNAPSHOT)|(-RC[0-9]+))?)?";
    static final String SIMPLE_PATTERN = "-x.x.x(-SNAPSHOT)(-RCx)";

    private final String prefix;
    private final boolean required;

    public JarDescription(String prefix, boolean required) {
        this.prefix = Objects.requireNonNull(prefix, "prefix");
        this.required = required;
    }

    public String getJarName() {
        return prefix.concat(".jar");
    }

    public Pattern getVersionPattern() {
        return Pattern.compile(prefix + VERSION_PATTERN + "\\.jar");
    }

    public String getSimplePattern() {
        return prefix + SIMPLE_PATTERN + ".jar";
    }

    public boolean isRequired() {
        return required;
    }
}
