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

package cn.polarismesh.agent.core.asm.instrument.plugin;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dubbo 版本探测器，从 classpath 或 fat JAR 中检测 Dubbo 版本并映射到对应的插件名称。
 */
public class DubboVersionDetector {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(DubboVersionDetector.class.getCanonicalName());

    private static final Pattern DUBBO_JAR_PATTERN = Pattern.compile("dubbo-(\\d+\\.\\d+\\.\\d+)\\.jar");

    /**
     * 检测 Dubbo 版本，先从 classpath 检测，再从 fat JAR 检测。
     *
     * @return Dubbo 版本字符串，未检测到则返回空字符串
     */
    public String detectVersion() {
        String version = detectFromClasspath();
        if (version.isEmpty()) {
            version = detectFromFatJar();
        }
        return version;
    }

    /**
     * 根据 Dubbo 版本号映射到对应的插件名称。
     *
     * @param version Dubbo 版本号，例如 "2.7.23"、"3.2.0"
     * @return 插件名称，无法映射则返回空字符串
     */
    public String getPluginName(String version) {
        if (version == null || version.isEmpty()) {
            return "";
        }
        if (version.startsWith("2.7.")) {
            return "dubbo-2.7.x-plugin";
        }
//        not support yet
//        if (version.startsWith("3.")) {
//            return "dubbo-3.x-plugin";
//        }
        return "";
    }

    /**
     * 从 JAR 文件名中提取 Dubbo 版本号。
     * 仅匹配形如 dubbo-x.y.z.jar 的文件名。
     *
     * @param fileName JAR 文件名
     * @return 版本号字符串，无法提取则返回空字符串
     */
    static String extractVersionFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        Matcher matcher = DUBBO_JAR_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 从 classpath 中检测 Dubbo 版本。
     * 扫描 java.class.path 系统属性，查找形如 dubbo-x.y.z.jar 的条目。
     *
     * @return Dubbo 版本字符串，未检测到则返回空字符串
     */
    static String detectFromClasspath() {
        String classPath = System.getProperty("java.class.path");
        if (classPath == null || classPath.isEmpty()) {
            return "";
        }
        String separator = System.getProperty("path.separator", ":");
        String[] paths = classPath.split(
                Pattern.quote(separator));
        for (String path : paths) {
            String fileName = extractFileName(path);
            String version = extractVersionFromFileName(fileName);
            if (!version.isEmpty()) {
                logger.info("Detected Dubbo version from classpath: "
                        + version + " (file: " + fileName + ")");
                return version;
            }
        }
        return "";
    }

    private static String extractFileName(String path) {
        int lastSep = path.lastIndexOf('/');
        if (lastSep < 0) {
            lastSep = path.lastIndexOf('\\');
        }
        if (lastSep >= 0) {
            return path.substring(lastSep + 1);
        }
        return path;
    }

    /**
     * 策略二：扫描 fat JAR 内部的 BOOT-INF/lib/ 条目.
     * 适用于 Spring Boot fat JAR 部署场景.
     *
     * @return Dubbo 版本字符串，未检测到则返回空字符串
     */
    static String detectFromFatJar() {
        String classPath = System.getProperty("java.class.path");
        if (classPath == null || classPath.isEmpty()) {
            return "";
        }
        String separator = System.getProperty("path.separator", ":");
        String mainJarPath = classPath.split(
                Pattern.quote(separator))[0];
        File mainJarFile = new File(mainJarPath);
        if (!mainJarFile.isFile()) {
            return "";
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(mainJarFile);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entryName.startsWith("BOOT-INF/lib/")) {
                    continue;
                }
                String fileName = entryName.substring(
                        entryName.lastIndexOf('/') + 1);
                String version =
                        extractVersionFromFileName(fileName);
                if (!version.isEmpty()) {
                    logger.info("Detected Dubbo version from"
                            + " fat JAR: " + version
                            + " (entry: " + entryName + ")");
                    return version;
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to scan fat JAR for Dubbo"
                    + " version: " + mainJarPath, e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    logger.warn("Failed to close JAR file", e);
                }
            }
        }
        return "";
    }
}
