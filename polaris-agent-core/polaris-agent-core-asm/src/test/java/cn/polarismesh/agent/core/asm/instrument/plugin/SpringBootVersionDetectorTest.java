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
 * SpringBootVersionDetector 单元测试。
 */
public class SpringBootVersionDetectorTest {

    private final SpringBootVersionDetector detector =
            new SpringBootVersionDetector();

    // ==================== getSpringCloudVersion 测试 ====================

    @Test
    public void testGetSpringCloudVersion_hoxton_22() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("2.2.13"))
                .isEqualTo("hoxton");
    }

    @Test
    public void testGetSpringCloudVersion_hoxton_23() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("2.3.12"))
                .isEqualTo("hoxton");
    }

    @Test
    public void testGetSpringCloudVersion_2021_26() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("2.6.15"))
                .isEqualTo("2021");
    }

    @Test
    public void testGetSpringCloudVersion_2021_27() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("2.7.18"))
                .isEqualTo("2021");
    }

    @Test
    public void testGetSpringCloudVersion_2022_30() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("3.0.13"))
                .isEqualTo("2022");
    }

    @Test
    public void testGetSpringCloudVersion_2022_31() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("3.1.12"))
                .isEqualTo("2022");
    }

    @Test
    public void testGetSpringCloudVersion_2023_32() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("3.2.5"))
                .isEqualTo("2023");
    }

    @Test
    public void testGetSpringCloudVersion_2023_33() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("3.3.0"))
                .isEqualTo("2023");
    }

    @Test
    public void testGetSpringCloudVersion_2024_34() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("3.4.0"))
                .isEqualTo("2024");
    }

    @Test
    public void testGetSpringCloudVersion_unsupported() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion("2.4.0"))
                .isEmpty();
    }

    @Test
    public void testGetSpringCloudVersion_empty() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion(""))
                .isEmpty();
    }

    @Test
    public void testGetSpringCloudVersion_null() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .getSpringCloudVersion(null))
                .isEmpty();
    }

    // ==================== getPluginName 测试 ====================

    @Test
    public void testGetPluginName_2023() {
        Assertions.assertThat(
                detector.getPluginName("3.2.5"))
                .isEqualTo("spring-cloud-2023-plugin");
    }

    @Test
    public void testGetPluginName_hoxton() {
        Assertions.assertThat(
                detector.getPluginName("2.3.12"))
                .isEqualTo("spring-cloud-hoxton-plugin");
    }

    @Test
    public void testGetPluginName_unsupported() {
        Assertions.assertThat(
                detector.getPluginName("2.4.0"))
                .isEmpty();
    }

    @Test
    public void testGetPluginName_empty() {
        Assertions.assertThat(
                detector.getPluginName(""))
                .isEmpty();
    }

    @Test
    public void testGetPluginName_null() {
        Assertions.assertThat(
                detector.getPluginName(null))
                .isEmpty();
    }

    // ========== extractVersionFromFileName 测试 ==========

    @Test
    public void testExtractVersion_springBoot325() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(
                                "spring-boot-3.2.5.jar"))
                .isEqualTo("3.2.5");
    }

    @Test
    public void testExtractVersion_snapshot() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(
                                "spring-boot-3.2.5-SNAPSHOT.jar"))
                .isEqualTo("3.2.5-SNAPSHOT");
    }

    @Test
    public void testExtractVersion_rc() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(
                                "spring-boot-3.3.0-RC1.jar"))
                .isEqualTo("3.3.0-RC1");
    }

    @Test
    public void testExtractVersion_autoconfigure_noMatch() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(
                                "spring-boot-autoconfigure-3.2.5.jar"))
                .isEmpty();
    }

    @Test
    public void testExtractVersion_actuator_noMatch() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(
                                "spring-boot-actuator-3.2.5.jar"))
                .isEmpty();
    }

    @Test
    public void testExtractVersion_starter_noMatch() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(
                                "spring-boot-starter-web-3.2.5.jar"))
                .isEmpty();
    }

    @Test
    public void testExtractVersion_loader_noMatch() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(
                                "spring-boot-loader-3.2.5.jar"))
                .isEmpty();
    }

    @Test
    public void testExtractVersion_null() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(null))
                .isEmpty();
    }

    @Test
    public void testExtractVersion_empty() {
        Assertions.assertThat(
                SpringBootVersionDetector
                        .extractVersionFromFileName(""))
                .isEmpty();
    }

    // ==================== detectFromClasspath 测试 ====================

    @Test
    public void testDetectFromClasspath_found() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/spring-core-6.1.6.jar"
                            + System.getProperty("path.separator")
                            + "/app/lib/spring-boot-3.2.5.jar"
                            + System.getProperty("path.separator")
                            + "/app/lib/spring-boot-autoconfigure-3.2.5.jar");
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromClasspath())
                    .isEqualTo("3.2.5");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromClasspath_skipsSubmodule() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/spring-boot-autoconfigure-3.2.5.jar"
                            + System.getProperty("path.separator")
                            + "/app/lib/spring-boot-actuator-3.2.5.jar");
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromClasspath())
                    .isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromClasspath_noSpringBoot() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/spring-core-6.1.6.jar"
                            + System.getProperty("path.separator")
                            + "/app/lib/dubbo-2.7.23.jar");
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromClasspath())
                    .isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromClasspath_withMavenRepoPath() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/home/user/.m2/repository/org/springframework"
                            + "/boot/spring-boot/3.2.5"
                            + "/spring-boot-3.2.5.jar");
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromClasspath())
                    .isEqualTo("3.2.5");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    // ==================== detectFromFatJar 测试 ====================

    /**
     * 创建模拟的 Spring Boot fat JAR.
     */
    private static File createFakeFatJar(
            String bootVersion,
            String... libEntries) throws Exception {
        File tempJar = File.createTempFile(
                "fake-springboot-", ".jar");
        tempJar.deleteOnExit();
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(
                Attributes.Name.MANIFEST_VERSION, "1.0");
        if (bootVersion != null) {
            manifest.getMainAttributes().putValue(
                    "Spring-Boot-Version", bootVersion);
        }
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
    public void testDetectFromFatJar_found() throws Exception {
        File fatJar = createFakeFatJar(null,
                "spring-core-6.1.6.jar",
                "spring-boot-3.2.5.jar",
                "spring-boot-autoconfigure-3.2.5.jar");
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    fatJar.getAbsolutePath());
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromFatJar())
                    .isEqualTo("3.2.5");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromFatJar_noSpringBoot()
            throws Exception {
        File fatJar = createFakeFatJar(null,
                "spring-core-6.1.6.jar",
                "dubbo-2.7.23.jar");
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    fatJar.getAbsolutePath());
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromFatJar())
                    .isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    // ============ detectFromManifest 测试 ============

    @Test
    public void testDetectFromManifest_found() throws Exception {
        File fatJar = createFakeFatJar("3.2.5");
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    fatJar.getAbsolutePath());
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromManifest())
                    .isEqualTo("3.2.5");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromManifest_noAttribute()
            throws Exception {
        File fatJar = createFakeFatJar(null);
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    fatJar.getAbsolutePath());
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromManifest())
                    .isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testDetectFromManifest_notJarFile() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "target/classes");
            Assertions.assertThat(
                    SpringBootVersionDetector
                            .detectFromManifest())
                    .isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }
}
