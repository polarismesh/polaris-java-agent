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

package cn.polarismesh.agent.plugin.dubbo27;

import cn.polarismesh.agent.plugin.dubbo27.constants.DubboConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * DubboConfigProvider 单元测试.
 */
public class DubboConfigProviderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @After
    public void tearDown() {
        System.clearProperty(DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS);
        System.clearProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY);
    }

    @Test
    public void testLoadDefaultConfig() {
        // 未设置系统属性时，返回默认 Polaris 地址
        System.clearProperty(
                DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS);
        String address =
                DubboConfigProvider.getPolarisServerAddress();
        Assertions.assertThat(address)
                .isEqualTo("polaris://127.0.0.1:8091");
    }

    @Test
    public void testLoadFromSystemProperty() {
        // 通过 -Ddubbo.registry.address 系统属性覆盖 Polaris 地址
        System.setProperty(
                DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS,
                "polaris://10.0.0.1:8091");
        String address =
                DubboConfigProvider.getPolarisServerAddress();
        Assertions.assertThat(address)
                .isEqualTo("polaris://10.0.0.1:8091");
    }

    @Test
    public void testLoadFromFile_address() throws IOException {
        // 系统属性未设置时，从配置文件读取地址
        System.clearProperty(
                DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS);
        File confDir =
                tempFolder.newFolder("conf", "plugin", "dubbo");
        File propsFile =
                new File(confDir, "dubbo-polaris.properties");
        try (FileWriter writer = new FileWriter(propsFile)) {
            writer.write(
                    "dubbo.registry.address=polaris://192.168.1.1:8091\n");
        }
        System.setProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY,
                tempFolder.getRoot().getAbsolutePath());

        String address =
                DubboConfigProvider.getPolarisServerAddress();

        Assertions.assertThat(address)
                .isEqualTo("polaris://192.168.1.1:8091");
    }

    @Test
    public void testSystemPropertyOverridesFile() throws IOException {
        // JVM 系统属性优先级高于配置文件
        System.setProperty(
                DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS,
                "polaris://from-sysprop:8091");
        File confDir =
                tempFolder.newFolder("conf", "plugin", "dubbo");
        File propsFile =
                new File(confDir, "dubbo-polaris.properties");
        try (FileWriter writer = new FileWriter(propsFile)) {
            writer.write(
                    "dubbo.registry.address=polaris://from-file:8091\n");
        }
        System.setProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY,
                tempFolder.getRoot().getAbsolutePath());

        String address =
                DubboConfigProvider.getPolarisServerAddress();

        Assertions.assertThat(address)
                .isEqualTo("polaris://from-sysprop:8091");
    }

    @Test
    public void testLoadFromFile_parameters() throws IOException {
        // 从配置文件读取 dubbo.registry.parameters.*，正确解析为 Map
        File confDir =
                tempFolder.newFolder("conf", "plugin", "dubbo");
        File propsFile =
                new File(confDir, "dubbo-polaris.properties");
        try (FileWriter writer = new FileWriter(propsFile)) {
            writer.write(
                    "dubbo.registry.address=polaris://127.0.0.1:8091\n");
            writer.write(
                    "dubbo.registry.parameters.polaris_nacos_enabled=true\n");
            writer.write(
                    "dubbo.registry.parameters.polaris_nacos_server_addr=127.0.0.1:8848\n");
        }
        System.setProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY,
                tempFolder.getRoot().getAbsolutePath());

        Map<String, String> params =
                DubboConfigProvider.getRegistryParameters();

        Assertions.assertThat(params)
                .containsEntry("polaris_nacos_enabled", "true");
        Assertions.assertThat(params).containsEntry(
                "polaris_nacos_server_addr", "127.0.0.1:8848");
        Assertions.assertThat(params)
                .doesNotContainKey("dubbo.registry.address");
    }

    @Test
    public void testGetRegistryParameters_emptyWhenNoFile() {
        // 文件不存在时返回空 Map
        System.clearProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY);

        Map<String, String> params =
                DubboConfigProvider.getRegistryParameters();

        Assertions.assertThat(params).isEmpty();
    }
}
