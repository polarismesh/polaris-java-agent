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
import cn.polarismesh.agent.core.common.utils.JarFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spring Boot 版本探测器，从 classpath 或 fat JAR 中检测 Spring Boot
 * 版本并映射到对应的 Spring Cloud 插件名称。
 *
 * <p>检测策略（按优先级）：
 * <ol>
 *   <li>读取主 JAR 的 MANIFEST.MF 中 Spring-Boot-Version 属性</li>
 *   <li>扫描 classpath 中的 spring-boot-{version}.jar 文件名</li>
 *   <li>扫描 fat JAR 内部 BOOT-INF/lib/ 中的
 *       spring-boot-{version}.jar 条目</li>
 * </ol>
 */
public class SpringBootVersionDetector {

    private static final CommonLogger logger =
            StdoutCommonLoggerFactory.INSTANCE.getLogger(
                    SpringBootVersionDetector.class
                            .getCanonicalName());

    private static final String SPRING_BOOT_VERSION =
            "Spring-Boot-Version";

    /**
     * 精确匹配 spring-boot-{version}.jar，排除
     * spring-boot-autoconfigure-{version}.jar 等子模块。
     * 原理：spring-boot- 后紧跟数字开头的版本号。
     */
    static final Pattern SPRING_BOOT_JAR_PATTERN =
            Pattern.compile(
                    "spring-boot-(\\d+\\.\\d+\\.\\d+[^/\\\\]*)\\.jar");

    /**
     * 依次尝试三种策略检测 Spring Boot 版本。
     *
     * @return Spring Boot 版本字符串，未检测到则返回空字符串
     */
    public String detectVersion() {
        String version = detectFromManifest();
        if (!version.isEmpty()) {
            return version;
        }
        version = detectFromClasspath();
        if (!version.isEmpty()) {
            return version;
        }
        return detectFromFatJar();
    }

    /**
     * 根据 Spring Boot 版本号映射到 Spring Cloud 插件名称。
     *
     * @param version Spring Boot 版本号
     * @return 插件名称，无法映射则返回空字符串
     */
    public String getPluginName(String version) {
        if (version == null || version.isEmpty()) {
            return "";
        }
        String cloudVersion = getSpringCloudVersion(version);
        if (cloudVersion.isEmpty()) {
            return "";
        }
        return String.format("spring-cloud-%s-plugin", cloudVersion);
    }

    /**
     * 将 Spring Boot 版本映射到 Spring Cloud 版本代号。
     */
    static String getSpringCloudVersion(String bootVersion) {
        if (bootVersion == null || bootVersion.isEmpty()) {
            return "";
        }
        if (bootVersion.startsWith("2.2")
                || bootVersion.startsWith("2.3")) {
            return "hoxton";
        }
        if (bootVersion.startsWith("2.6")
                || bootVersion.startsWith("2.7")) {
            return "2021";
        }
        if (bootVersion.startsWith("3.0")
                || bootVersion.startsWith("3.1")) {
            return "2022";
        }
        if (bootVersion.startsWith("3.2")
                || bootVersion.startsWith("3.3")) {
            return "2023";
        }
        if (bootVersion.startsWith("3.4")) {
            return "2024";
        }
        return "";
    }

    /**
     * 策略一：从主 JAR 的 MANIFEST.MF 中读取
     * Spring-Boot-Version 属性。仅在 fat JAR 场景下有效。
     *
     * @return Spring Boot 版本字符串，未检测到则返回空字符串
     */
    static String detectFromManifest() {
        String classPath = System.getProperty("java.class.path");
        if (classPath == null || classPath.isEmpty()) {
            return "";
        }
        String separator =
                System.getProperty("path.separator", ":");
        String mainJarPath =
                classPath.split(Pattern.quote(separator))[0];
        File mainJarFile = new File(mainJarPath);
        if (!mainJarFile.isFile()) {
            return "";
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(mainJarFile);
            String version = JarFileUtils.getManifestValue(
                    jarFile, SPRING_BOOT_VERSION, "");
            if (version != null && !version.isEmpty()) {
                logger.info(
                        "Detected Spring Boot version from"
                                + " MANIFEST: " + version);
                return version;
            }
        } catch (IOException e) {
            logger.warn("Cannot read MANIFEST from: "
                    + mainJarPath, e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    logger.warn("Cannot close jarFile", e);
                }
            }
        }
        return "";
    }

    /**
     * 策略二：扫描 classpath 中的 spring-boot-{version}.jar
     * 文件名。适用于 IDE 启动或 exploded classpath 场景。
     *
     * @return Spring Boot 版本字符串，未检测到则返回空字符串
     */
    static String detectFromClasspath() {
        String classPath = System.getProperty("java.class.path");
        if (classPath == null || classPath.isEmpty()) {
            return "";
        }
        String separator =
                System.getProperty("path.separator", ":");
        String[] paths =
                classPath.split(Pattern.quote(separator));
        for (String path : paths) {
            String fileName = extractFileName(path);
            String version = extractVersionFromFileName(fileName);
            if (!version.isEmpty()) {
                logger.info(
                        "Detected Spring Boot version from"
                                + " classpath: " + version
                                + " (file: " + fileName + ")");
                return version;
            }
        }
        return "";
    }

    /**
     * 策略三：扫描 fat JAR 内部 BOOT-INF/lib/ 条目。
     * 作为兜底策略，适用于 MANIFEST 中没有
     * Spring-Boot-Version 的特殊打包场景。
     *
     * @return Spring Boot 版本字符串，未检测到则返回空字符串
     */
    static String detectFromFatJar() {
        String classPath = System.getProperty("java.class.path");
        if (classPath == null || classPath.isEmpty()) {
            return "";
        }
        String separator =
                System.getProperty("path.separator", ":");
        String mainJarPath =
                classPath.split(Pattern.quote(separator))[0];
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
                    logger.info(
                            "Detected Spring Boot version from"
                                    + " fat JAR: " + version
                                    + " (entry: "
                                    + entryName + ")");
                    return version;
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to scan fat JAR for Spring Boot"
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

    /**
     * 从 JAR 文件名中提取 Spring Boot 版本号。
     * 仅匹配 spring-boot-{version}.jar，排除子模块。
     *
     * @param fileName JAR 文件名
     * @return 版本号字符串，无法提取则返回空字符串
     */
    static String extractVersionFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        Matcher matcher =
                SPRING_BOOT_JAR_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return matcher.group(1);
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
}
