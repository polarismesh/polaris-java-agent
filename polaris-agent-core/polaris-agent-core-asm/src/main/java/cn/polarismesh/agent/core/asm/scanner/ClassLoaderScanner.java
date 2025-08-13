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

package cn.polarismesh.agent.core.asm.scanner;

import java.io.InputStream;
import java.util.Objects;

public class ClassLoaderScanner implements Scanner {

    private final ClassLoader classLoader;

    public ClassLoaderScanner(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        this.classLoader = classLoader;
    }

    @Override
    public boolean exist(String fileName) {
        Objects.requireNonNull(fileName, "fileName");

        // TODO
        return classLoader.getResource(fileName) != null;
    }

    @Override
    public InputStream openStream(String fileName) {
        Objects.requireNonNull(fileName, "fileName");

        return classLoader.getResourceAsStream(fileName);
    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return "ClassLoaderScanner{" +
                "classLoader=" + classLoader +
                '}';
    }
}
