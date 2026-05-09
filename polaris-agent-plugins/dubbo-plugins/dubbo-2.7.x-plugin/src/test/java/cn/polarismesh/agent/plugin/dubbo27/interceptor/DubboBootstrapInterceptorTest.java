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

package cn.polarismesh.agent.plugin.dubbo27.interceptor;

import cn.polarismesh.agent.plugin.dubbo27.DubboPropertiesLoader;
import cn.polarismesh.agent.plugin.dubbo27.constants.DubboConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * DubboBootstrapInterceptor 单元测试.
 */
public class DubboBootstrapInterceptorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private final DubboBootstrapInterceptor interceptor =
            new DubboBootstrapInterceptor();

    @Before
    public void setUp() {
        ApplicationModel.getConfigManager().clear();
    }

    @After
    public void tearDown() {
        System.clearProperty(
                DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS);
        System.clearProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY);
        ApplicationModel.getConfigManager().clear();
    }

    @Test
    public void testReplaceZookeeperRegistry() {
        // zookeeper 注册中心应被替换为 polaris
        RegistryConfig zkRegistry = new RegistryConfig();
        zkRegistry.setProtocol("zookeeper");
        zkRegistry.setAddress("zookeeper://127.0.0.1:2181");
        ApplicationModel.getConfigManager()
                .addRegistry(zkRegistry);

        interceptor.after(new Object(), null, null, null);

        Assertions.assertThat(zkRegistry.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(zkRegistry.getAddress())
                .isEqualTo("polaris://127.0.0.1:8091");
    }

    @Test
    public void testSkipAlreadyPolarisRegistry() {
        // 已经是 polaris 协议的注册中心不应被修改
        RegistryConfig polarisReg = new RegistryConfig();
        polarisReg.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        polarisReg.setAddress("polaris://10.0.0.1:8091");
        ApplicationModel.getConfigManager()
                .addRegistry(polarisReg);

        interceptor.after(new Object(), null, null, null);

        Assertions.assertThat(polarisReg.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(polarisReg.getAddress())
                .isEqualTo("polaris://10.0.0.1:8091");
    }

    @Test
    public void testReplaceMultipleRegistries() {
        // 多个注册中心都应被替换
        RegistryConfig firstReg = new RegistryConfig();
        firstReg.setId("reg1");
        firstReg.setProtocol("zookeeper");
        firstReg.setAddress("zookeeper://host1:2181");
        RegistryConfig secondReg = new RegistryConfig();
        secondReg.setId("reg2");
        secondReg.setProtocol("nacos");
        secondReg.setAddress("nacos://host2:8848");
        ConfigManager configManager =
                ApplicationModel.getConfigManager();
        configManager.addRegistry(firstReg);
        configManager.addRegistry(secondReg);

        interceptor.after(new Object(), null, null, null);

        Assertions.assertThat(firstReg.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(secondReg.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }

    @Test
    public void testEmptyRegistriesAddsPolarisConfig() {
        // ConfigManager 中无注册中心时，应自动添加 polaris 注册配置
        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> registries =
                ApplicationModel.getConfigManager().getRegistries();
        Assertions.assertThat(registries).hasSize(1);
        RegistryConfig added = registries.iterator().next();
        Assertions.assertThat(added.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }

    @Test
    public void testCustomPolarisAddressViaSystemProperty() {
        // 通过 -Ddubbo.registry.address 系统属性指定自定义地址
        System.setProperty(
                DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS,
                "polaris://10.0.0.1:8091");

        RegistryConfig zkReg = new RegistryConfig();
        zkReg.setProtocol("zookeeper");
        zkReg.setAddress("zookeeper://127.0.0.1:2181");
        ApplicationModel.getConfigManager().addRegistry(zkReg);

        interceptor.after(new Object(), null, null, null);

        Assertions.assertThat(zkReg.getAddress())
                .isEqualTo("polaris://10.0.0.1:8091");
    }

    @Test
    public void testAfter_setsParametersWhenConfigured()
            throws IOException {
        // 配置了 parameters 时，registry.setParameters() 被调用
        File confDir = tempFolder.newFolder(
                "conf", "plugin", "dubbo");
        File propsFile = new File(
                confDir, "dubbo-polaris.properties");
        try (FileWriter writer = new FileWriter(propsFile)) {
            writer.write("dubbo.registry.address="
                    + "polaris://127.0.0.1:8091\n");
            writer.write("dubbo.registry.parameters"
                    + ".polaris_nacos_enabled=true\n");
        }
        System.setProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY,
                tempFolder.getRoot().getAbsolutePath());

        RegistryConfig zkReg = new RegistryConfig();
        zkReg.setProtocol("zookeeper");
        zkReg.setAddress("zookeeper://127.0.0.1:2181");
        ApplicationModel.getConfigManager().addRegistry(zkReg);

        interceptor.after(new Object(), null, null, null);

        Assertions.assertThat(zkReg.getParameters())
                .isNotNull()
                .containsEntry("polaris_nacos_enabled", "true");
    }

    @Test
    public void testAfter_skipsParametersWhenEmpty() {
        // parameters 为空时，不应设置
        System.clearProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY);

        RegistryConfig zkReg = new RegistryConfig();
        zkReg.setProtocol("zookeeper");
        zkReg.setAddress("zookeeper://127.0.0.1:2181");
        ApplicationModel.getConfigManager().addRegistry(zkReg);

        interceptor.after(new Object(), null, null, null);

        Map<String, String> params = zkReg.getParameters();
        Assertions.assertThat(
                params == null || params.isEmpty()).isTrue();
    }

    @Test
    public void testAfter_mergesParametersWithExisting()
            throws IOException {
        // 已有 parameters 应被保留，同名 key 被配置文件覆盖
        File confDir = tempFolder.newFolder(
                "conf", "plugin", "dubbo");
        File propsFile = new File(
                confDir, "dubbo-polaris.properties");
        try (FileWriter writer = new FileWriter(propsFile)) {
            writer.write("dubbo.registry.address="
                    + "polaris://127.0.0.1:8091\n");
            writer.write("dubbo.registry.parameters"
                    + ".polaris_nacos_enabled=true\n");
            writer.write("dubbo.registry.parameters"
                    + ".existing_key=new_value\n");
        }
        System.setProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY,
                tempFolder.getRoot().getAbsolutePath());

        RegistryConfig zkReg = new RegistryConfig();
        zkReg.setProtocol("zookeeper");
        zkReg.setAddress("zookeeper://127.0.0.1:2181");
        Map<String, String> existingParams =
                new HashMap<String, String>();
        existingParams.put("existing_key", "old_value");
        existingParams.put("keep_me", "untouched");
        zkReg.setParameters(existingParams);
        ApplicationModel.getConfigManager().addRegistry(zkReg);

        interceptor.after(new Object(), null, null, null);

        Map<String, String> result = zkReg.getParameters();
        Assertions.assertThat(result)
                .containsEntry("polaris_nacos_enabled", "true")
                .containsEntry("existing_key", "new_value")
                .containsEntry("keep_me", "untouched");
    }
}
