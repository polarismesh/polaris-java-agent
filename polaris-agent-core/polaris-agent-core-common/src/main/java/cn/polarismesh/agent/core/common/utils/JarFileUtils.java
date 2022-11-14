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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class JarFileUtils {

    private JarFileUtils() {
    }

    public static String getManifestValue(JarFile jarFile, String key, String defaultValue) {
        final Manifest manifest = getManifest(jarFile);
        if (manifest == null) {
            return defaultValue;
        }

        final Attributes attributes = manifest.getMainAttributes();
        final String value = attributes.getValue(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }


    public static Manifest getManifest(JarFile pluginJarFile) {
        try {
            return pluginJarFile.getManifest();
        } catch (IOException ex) {
            return null;
        }
    }

    public static JarFile openJarFile(String filePath) {
        Objects.requireNonNull(filePath, "filePath");

        final File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " not found");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is directory");
        }
        if (!(file.isFile())) {
            throw new IllegalArgumentException(file + " not file");
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException(file + " can read");
        }
        try {
            return new JarFile(file);
        } catch (IOException e) {
            throw new IllegalStateException(file + " create fail Caused by:" + e.getMessage(), e);
        }
    }

    public static List<JarFile> openJarFiles(List<String> jars) {
        final List<JarFile> jarFileList = new ArrayList<>(jars.size());
        for (String jarPath : jars) {
            final JarFile jarFile = JarFileUtils.openJarFile(jarPath);
            jarFileList.add(jarFile);
        }
        return jarFileList;
    }
}
