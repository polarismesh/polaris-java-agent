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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JarFileRepository {

    private final JarFileScanner[] scanners;


    public JarFileRepository(List<String> jarFilePathList) {
        this.scanners = newJarScanner(jarFilePathList);
    }

    private JarFileScanner[] newJarScanner(List<String> jarFilePathList) {
        Objects.requireNonNull(jarFilePathList, "jarFilePathList");

        final List<JarFileScanner> jarFileList = new ArrayList<>(jarFilePathList.size());
        for (String jarFilePath : jarFilePathList) {
            JarFileScanner jarFileScanner = new JarFileScanner(jarFilePath);
            jarFileList.add(jarFileScanner);
        }
        return jarFileList.toArray(new JarFileScanner[0]);
    }


    public InputStream openStream(String resourceName) {
        Objects.requireNonNull(resourceName, "resourceName");

        for (JarFileScanner scanner : scanners) {
            final InputStream inputStream = scanner.openStream(resourceName);
            if (inputStream != null) {
                return inputStream;
            }
        }
        return null;
    }

    public void close() {
        for (JarFileScanner scanner : scanners) {
            scanner.close();
        }
    }


}
