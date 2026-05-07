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

import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * DubboVersionDetector 单元测试。
 */
public class DubboVersionDetectorTest {

    private final DubboVersionDetector detector = new DubboVersionDetector();

    // ==================== getPluginName 测试 ====================

    @Test
    public void testGetPluginName_dubbo27x_full() {
        Assertions.assertThat(detector.getPluginName("2.7.23")).isEqualTo("dubbo-2.7.x-plugin");
    }

    @Test
    public void testGetPluginName_dubbo27x_min() {
        Assertions.assertThat(detector.getPluginName("2.7.0")).isEqualTo("dubbo-2.7.x-plugin");
    }

    @Test
    public void testGetPluginName_unsupported() {
        Assertions.assertThat(detector.getPluginName("2.6.9")).isEqualTo("");
    }

    @Test
    public void testGetPluginName_empty() {
        Assertions.assertThat(detector.getPluginName("")).isEqualTo("");
    }

    @Test
    public void testGetPluginName_null() {
        Assertions.assertThat(detector.getPluginName(null)).isEqualTo("");
    }

    // ==================== extractVersionFromFileName 测试 ====================

    @Test
    public void testExtractVersion_dubbo27() {
        Assertions.assertThat(detector.extractVersionFromFileName("dubbo-2.7.23.jar"))
                .isEqualTo("2.7.23");
    }

    @Test
    public void testExtractVersion_dubbo3() {
        Assertions.assertThat(detector.extractVersionFromFileName("dubbo-3.2.0.jar"))
                .isEqualTo("3.2.0");
    }

    @Test
    public void testExtractVersion_dubboCommon_noMatch() {
        Assertions.assertThat(detector.extractVersionFromFileName("dubbo-common-2.7.23.jar"))
                .isEqualTo("");
    }

    @Test
    public void testExtractVersion_otherJar_noMatch() {
        Assertions.assertThat(detector.extractVersionFromFileName("dubbo-registry-polaris-2.1.1.jar"))
                .isEqualTo("");
    }

    // ==================== detectFromClasspath 测试 ====================

    @Test
    public void testDetectFromClasspathWithDubboJar() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/spring-core-5.3.jar"
                            + System.getProperty("path.separator")
                            + "/app/lib/dubbo-2.7.23.jar"
                            + System.getProperty("path.separator")
                            + "/app/lib/netty-4.1.jar");
            Assertions.assertThat(
                    DubboVersionDetector.detectFromClasspath())
                    .isEqualTo("2.7.23");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromClasspathWithDubbo3Jar() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/dubbo-3.2.0.jar");
            Assertions.assertThat(
                    DubboVersionDetector.detectFromClasspath())
                    .isEqualTo("3.2.0");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromClasspathNoDubbo() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/spring-core-5.3.jar"
                            + System.getProperty("path.separator")
                            + "/app/lib/netty-4.1.jar");
            Assertions.assertThat(
                    DubboVersionDetector.detectFromClasspath())
                    .isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromClasspathSkipsSubmodule() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/dubbo-common-2.7.23.jar");
            Assertions.assertThat(
                    DubboVersionDetector.detectFromClasspath())
                    .isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    // ==================== detectFromFatJar 测试 ====================

    /**
     * 创建模拟的 Spring Boot fat JAR，包含指定的 BOOT-INF/lib/ 条目.
     */
    private static File createFakeFatJar(
            String... libEntries) throws Exception {
        File tempJar = File.createTempFile("fake-app-", ".jar");
        tempJar.deleteOnExit();
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(
                Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue(
                "Spring-Boot-Version", "2.7.18");
        JarOutputStream jos = new JarOutputStream(
                new FileOutputStream(tempJar), manifest);
        for (String entry : libEntries) {
            jos.putNextEntry(new JarEntry(
                    "BOOT-INF/lib/" + entry));
            jos.closeEntry();
        }
        jos.close();
        return tempJar;
    }

    @Test
    public void testDetectFromFatJarWithDubbo() throws Exception {
        File fatJar = createFakeFatJar(
                "spring-core-5.3.jar",
                "dubbo-2.7.23.jar",
                "dubbo-common-2.7.23.jar");
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    fatJar.getAbsolutePath());
            Assertions.assertThat(
                    DubboVersionDetector.detectFromFatJar())
                    .isEqualTo("2.7.23");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromFatJarWithDubbo3() throws Exception {
        File fatJar = createFakeFatJar(
                "dubbo-3.2.0.jar",
                "dubbo-common-3.2.0.jar");
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    fatJar.getAbsolutePath());
            Assertions.assertThat(
                    DubboVersionDetector.detectFromFatJar())
                    .isEqualTo("3.2.0");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromFatJarNoDubbo() throws Exception {
        File fatJar = createFakeFatJar(
                "spring-core-5.3.jar",
                "netty-4.1.jar");
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    fatJar.getAbsolutePath());
            Assertions.assertThat(
                    DubboVersionDetector.detectFromFatJar())
                    .isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }
}
