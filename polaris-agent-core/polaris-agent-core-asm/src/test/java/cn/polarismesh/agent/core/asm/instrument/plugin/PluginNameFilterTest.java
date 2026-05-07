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

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * PluginNameFilter Dubbo 自动检测相关单元测试.
 */
public class PluginNameFilterTest {

    @Test
    public void testAppendDubboPluginWhenAlreadyConfigured() {
        String result = PluginNameFilter
                .appendDubboPluginNameIfNeeded(
                        "dubbo-2.7.x-plugin");
        Assertions.assertThat(result)
                .isEqualTo("dubbo-2.7.x-plugin");
    }

    @Test
    public void testAppendDubboPluginWhenAlreadyInList() {
        String result = PluginNameFilter
                .appendDubboPluginNameIfNeeded(
                        "spring-cloud-2023-plugin,dubbo-2.7.x-plugin");
        Assertions.assertThat(result).isEqualTo(
                "spring-cloud-2023-plugin,dubbo-2.7.x-plugin");
    }

    @Test
    public void testAppendDubboPluginFromClasspath() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/dubbo-2.7.23.jar");
            String result = PluginNameFilter
                    .appendDubboPluginNameIfNeeded("");
            Assertions.assertThat(result)
                    .isEqualTo("dubbo-2.7.x-plugin");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testAppendDubboPluginAppendsToExisting() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/dubbo-2.7.23.jar");
            String result = PluginNameFilter
                    .appendDubboPluginNameIfNeeded(
                            "spring-cloud-2023-plugin");
            Assertions.assertThat(result).isEqualTo(
                    "spring-cloud-2023-plugin,dubbo-2.7.x-plugin");
        } finally {
            System.setProperty("java.class.path", original);
        }
    }

    @Test
    public void testAppendDubboPluginNoDubboOnClasspath() {
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    "/app/lib/spring-core-5.3.jar");
            String result = PluginNameFilter
                    .appendDubboPluginNameIfNeeded("");
            Assertions.assertThat(result).isEmpty();
        } finally {
            System.setProperty("java.class.path", original);
        }
    }
}
