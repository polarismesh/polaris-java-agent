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

import java.util.Objects;

public class FileBinary {

    private final String className;

    private final byte[] fileBinary;

    FileBinary(String fileName, byte[] fileBinary) {
        this.className = Objects.requireNonNull(fileName, "fileName");
        this.fileBinary = Objects.requireNonNull(fileBinary, "fileBinary");
    }

    public String getFileName() {
        return className;
    }

    public byte[] getFileBinary() {
        return fileBinary;
    }

    @Override
    public String toString() {
        return "FileBinary{" +
                "className='" + className + '\'' +
                ", fileBinarySize=" + fileBinary.length +
                '}';
    }
}
