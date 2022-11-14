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

package cn.polarismesh.agent.core.asm.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class DirectoryScanner implements Scanner {

    private final String directory;

    public DirectoryScanner(String directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
    }

    @Override
    public boolean exist(String fileName) {
        Objects.requireNonNull(fileName, "fileName");

        final String fullPath = getFullPath(fileName);
        final File file = new File(fullPath);

        return file.isFile();
    }

    private String getFullPath(String fileName) {
        return directory + fileName;
    }

    @Override
    public InputStream openStream(String fileName) {
        Objects.requireNonNull(fileName, "fileName");

        final String fullPath = getFullPath(fileName);
        try {
            return new FileInputStream(fullPath);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return "DirectoryScanner{" +
                "directory='" + directory + '\'' +
                '}';
    }
}
