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
        // 已有 polaris registry → agent 完全 no-op:
        // 对象引用、protocol、address 全部不变。
        RegistryConfig polarisReg = new RegistryConfig();
        polarisReg.setId("user-polaris");
        polarisReg.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        polarisReg.setAddress("polaris://10.0.0.1:8091");
        ApplicationModel.getConfigManager().addRegistry(polarisReg);

        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> all =
                ApplicationModel.getConfigManager().getRegistries();
        Assertions.assertThat(all).hasSize(1);
        // 对象引用未被替换
        Assertions.assertThat(all.iterator().next()).isSameAs(polarisReg);
        // 字段全部未动
        Assertions.assertThat(polarisReg.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(polarisReg.getAddress())
                .isEqualTo("polaris://10.0.0.1:8091");
        Assertions.assertThat(polarisReg.getId())
                .isEqualTo("user-polaris");
    }

    @Test
    public void testAfter_polarisOnly_skipsRewrite() {
        // 仅 1 个 polaris registry → no-op (与 testSkipAlreadyPolarisRegistry 同语义,
        // 但加入了 parameters 与自定义 address 验证)
        RegistryConfig userPolaris = new RegistryConfig();
        userPolaris.setId("custom-id");
        userPolaris.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        userPolaris.setAddress("polaris://my-polaris.example.com:8091");
        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("custom_param", "custom_value");
        userPolaris.setParameters(userParams);
        ApplicationModel.getConfigManager().addRegistry(userPolaris);

        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> all =
                ApplicationModel.getConfigManager().getRegistries();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next()).isSameAs(userPolaris);
        Assertions.assertThat(userPolaris.getAddress())
                .isEqualTo("polaris://my-polaris.example.com:8091");
        Assertions.assertThat(userPolaris.getParameters())
                .containsEntry("custom_param", "custom_value");
    }

    @Test
    public void testAfter_polarisPlusNacos_skipsRewriteBoth() {
        // polaris + nacos 共存 → 两个都不改、不删 (核心新语义,vs 旧 single 模式删 nacos)
        RegistryConfig polaris = new RegistryConfig();
        polaris.setId("user-polaris");
        polaris.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        polaris.setAddress("polaris://10.0.0.1:8091");
        RegistryConfig nacos = new RegistryConfig();
        nacos.setId("user-nacos");
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://1.2.3.4:8848");
        ConfigManager configManager =
                ApplicationModel.getConfigManager();
        configManager.addRegistry(polaris);
        configManager.addRegistry(nacos);

        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> all = configManager.getRegistries();
        Assertions.assertThat(all).hasSize(2);
        // 两个原对象都还在
        Assertions.assertThat(all).contains(polaris, nacos);
        // 字段全部未动
        Assertions.assertThat(polaris.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(polaris.getAddress())
                .isEqualTo("polaris://10.0.0.1:8091");
        Assertions.assertThat(nacos.getProtocol()).isEqualTo("nacos");
        Assertions.assertThat(nacos.getAddress())
                .isEqualTo("nacos://1.2.3.4:8848");
    }

    @Test
    public void testMultipleRegistries_keepsFirstZeroRemovesRest() {
        // 多个非-polaris registry → 改写 id="...RegistryConfig#0" 那条为 polaris,
        // 其他全部删除。验证 pickFirst 优先选 #0 而非 HashMap iter().next()。
        RegistryConfig nacos = new RegistryConfig();
        nacos.setId(DubboConstants.AUTO_REGISTRY_ID_PREFIX_ZERO);
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://1.2.3.4:8848");
        RegistryConfig zk = new RegistryConfig();
        zk.setId("org.apache.dubbo.config.RegistryConfig#1");
        zk.setProtocol("zookeeper");
        zk.setAddress("zookeeper://5.6.7.8:2181");
        ConfigManager configManager =
                ApplicationModel.getConfigManager();
        configManager.addRegistry(nacos);
        configManager.addRegistry(zk);

        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> remaining =
                configManager.getRegistries();
        Assertions.assertThat(remaining).hasSize(1);
        RegistryConfig survivor = remaining.iterator().next();
        // 改写的就是 #0 那条对象本身 (内存引用不变,仅字段变)
        Assertions.assertThat(survivor).isSameAs(nacos);
        Assertions.assertThat(survivor.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(survivor.getAddress())
                .isEqualTo("polaris://127.0.0.1:8091");
    }

    @Test
    public void testAfter_pickFirstRegistryFallback_whenNoZeroId() {
        // 多个 registry 但都不是 #0 id → fallback 到 iterator().next()
        // (HashMap 顺序不稳定,只断言"剩 1 个 polaris" + "无异常")。
        RegistryConfig nacos = new RegistryConfig();
        nacos.setId("my-nacos");
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://1.2.3.4:8848");
        RegistryConfig zk = new RegistryConfig();
        zk.setId("my-zk");
        zk.setProtocol("zookeeper");
        zk.setAddress("zookeeper://5.6.7.8:2181");
        ConfigManager configManager =
                ApplicationModel.getConfigManager();
        configManager.addRegistry(nacos);
        configManager.addRegistry(zk);

        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> remaining =
                configManager.getRegistries();
        Assertions.assertThat(remaining).hasSize(1);
        Assertions.assertThat(remaining.iterator().next().getProtocol())
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
