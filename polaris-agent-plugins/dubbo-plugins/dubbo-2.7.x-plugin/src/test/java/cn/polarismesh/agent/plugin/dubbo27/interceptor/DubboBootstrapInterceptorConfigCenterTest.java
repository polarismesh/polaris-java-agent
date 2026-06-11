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

import java.util.Collection;

import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * DubboBootstrapInterceptor#before() (config-center 改写) 单元测试.
 */
public class DubboBootstrapInterceptorConfigCenterTest {

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
                DubboConstants.KEY_DUBBO_CONFIG_CENTER_ADDRESS);
        System.clearProperty(
                DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED);
        System.clearProperty(
                DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY);
        ApplicationModel.getConfigManager().clear();
    }

    @Test
    public void testBefore_emptyConfigCenters_addsPolaris() {
        // 无 ConfigCenter 时,应新增一个 polaris ConfigCenter,协议=polaris、地址=默认 8093。
        // 注意:Dubbo 2.7.x ConfigCenterConfig 的 namespace/group 默认值为 "dubbo"
        // (CommonConstants.DUBBO),plugin 不设置它们,保留 Dubbo 默认值即可。
        interceptor.before(new Object(), null);

        Collection<ConfigCenterConfig> centers =
                ApplicationModel.getConfigManager().getConfigCenters();
        Assertions.assertThat(centers).hasSize(1);
        ConfigCenterConfig added = centers.iterator().next();
        Assertions.assertThat(added.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(added.getAddress())
                .isEqualTo("polaris://127.0.0.1:8093");
        // Dubbo 2.7.23 default: namespace="dubbo", plugin must NOT override it
        Assertions.assertThat(added.getNamespace()).isEqualTo("dubbo");
        // Dubbo 2.7.23 default: group="dubbo", plugin must NOT override it
        Assertions.assertThat(added.getGroup()).isEqualTo("dubbo");
    }

    @Test
    public void testBefore_existingNacos_rewriteToPolaris() {
        // 已有 nacos ConfigCenter,带 query 参数和用户业务 namespace/group。
        // 改写后:
        //   - protocol/address 改为 polaris
        //   - port/cluster/username/password 清掉
        //   - parameters 重置 (无 nacos 残留)
        //   - namespace/group 保留用户业务值
        ConfigCenterConfig nacos = new ConfigCenterConfig();
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://1.2.3.4:8848?username=admin&group=DEFAULT_GROUP");
        nacos.setNamespace("my-app-ns");
        nacos.setGroup("my-app-group");
        nacos.setUsername("admin");
        nacos.setPassword("secret");
        ApplicationModel.getConfigManager().addConfigCenter(nacos);

        interceptor.before(new Object(), null);

        Assertions.assertThat(nacos.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(nacos.getAddress())
                .isEqualTo("polaris://127.0.0.1:8093");
        Assertions.assertThat(nacos.getPort()).isNull();
        Assertions.assertThat(nacos.getCluster()).isNull();
        Assertions.assertThat(nacos.getUsername()).isNull();
        Assertions.assertThat(nacos.getPassword()).isNull();
        Assertions.assertThat(nacos.getParameters())
                .doesNotContainKey("username")
                .doesNotContainKey("group");
        // namespace / group 保留用户业务语义
        Assertions.assertThat(nacos.getNamespace()).isEqualTo("my-app-ns");
        Assertions.assertThat(nacos.getGroup()).isEqualTo("my-app-group");
    }

    @Test
    public void testBefore_existingNacosWithNullNamespaceGroup_keepsNull() {
        // 用户显式设置 namespace/group 为 null (覆盖 Dubbo 默认的 "dubbo"),
        // 改写后 plugin 不填默认值,仍保持 null;由 PolarisDynamicConfigurationFactory 兜底。
        ConfigCenterConfig nacos = new ConfigCenterConfig();
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://1.2.3.4:8848");
        nacos.setNamespace(null);  // 明确覆盖 Dubbo 默认的 "dubbo"
        nacos.setGroup(null);       // 明确覆盖 Dubbo 默认的 "dubbo"
        ApplicationModel.getConfigManager().addConfigCenter(nacos);

        interceptor.before(new Object(), null);

        Assertions.assertThat(nacos.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(nacos.getNamespace()).isNull();
        Assertions.assertThat(nacos.getGroup()).isNull();
    }

    @Test
    public void testBefore_existingPolaris_unchanged() {
        // 已经是 polaris 的 ConfigCenterConfig 不应被修改
        ConfigCenterConfig polarisCc = new ConfigCenterConfig();
        polarisCc.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        polarisCc.setAddress("polaris://10.0.0.1:8093");
        polarisCc.setNamespace("custom-ns");
        polarisCc.setGroup("custom-group");
        ApplicationModel.getConfigManager().addConfigCenter(polarisCc);

        interceptor.before(new Object(), null);

        Assertions.assertThat(polarisCc.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(polarisCc.getAddress())
                .isEqualTo("polaris://10.0.0.1:8093");
        Assertions.assertThat(polarisCc.getNamespace()).isEqualTo("custom-ns");
        Assertions.assertThat(polarisCc.getGroup()).isEqualTo("custom-group");
    }

    @Test
    public void testBefore_mixedPolarisAndNacos_skipsRewrite() {
        // 同时有 polaris 和 nacos: plugin 选择不越权——发现已存在 polaris ConfigCenter
        // 即直接 no-op,既不改写 nacos,也不删除 nacos。语义是"用户自己已经显式声明了
        // polaris 配置,我们保持现状,任何混合配置都视为用户故意为之"。
        ConfigCenterConfig polarisCc = new ConfigCenterConfig();
        polarisCc.setId("cc-polaris");
        polarisCc.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        polarisCc.setAddress("polaris://10.0.0.1:8093");
        polarisCc.setNamespace("polaris-ns");

        ConfigCenterConfig nacos = new ConfigCenterConfig();
        nacos.setId("cc-nacos");
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://2.2.2.2:8848");

        ApplicationModel.getConfigManager().addConfigCenter(polarisCc);
        ApplicationModel.getConfigManager().addConfigCenter(nacos);

        interceptor.before(new Object(), null);

        // polaris 不变
        Assertions.assertThat(polarisCc.getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(polarisCc.getAddress())
                .isEqualTo("polaris://10.0.0.1:8093");
        Assertions.assertThat(polarisCc.getNamespace())
                .isEqualTo("polaris-ns");
        // nacos 也不变 (不改写、不删除)
        Assertions.assertThat(nacos.getProtocol()).isEqualTo("nacos");
        Assertions.assertThat(nacos.getAddress())
                .isEqualTo("nacos://2.2.2.2:8848");
        // ConfigManager 仍保有两条 ConfigCenter
        Collection<ConfigCenterConfig> centers =
                ApplicationModel.getConfigManager().getConfigCenters();
        Assertions.assertThat(centers).hasSize(2);
    }

    @Test
    public void testBefore_throwingConfigCenter_swallowed() {
        // 任何运行时异常 (此处用 setProtocol 抛 RuntimeException 模拟) 都不应向上传播,
        // 否则 DubboBootstrap.initialize() 会失败 → 应用启动失败。
        ConfigCenterConfig hostile = new ConfigCenterConfig() {
            @Override
            public void setProtocol(String protocol) {
                throw new RuntimeException("simulated failure");
            }
        };
        hostile.setAddress("nacos://1.2.3.4:8848");
        // 注意: 此时 hostile.getProtocol() 仍是 nacos (setAddress 内部走 URL.valueOf 解析回填),
        // 所以会进入 for 循环并触发我们 override 的 setProtocol 抛异常。
        ApplicationModel.getConfigManager().addConfigCenter(hostile);

        // 不应抛任何异常
        interceptor.before(new Object(), null);

        // 验证: hostile 仍在 ConfigManager 里 (没被 plugin 删掉)
        Collection<ConfigCenterConfig> centers =
                ApplicationModel.getConfigManager().getConfigCenters();
        Assertions.assertThat(centers).hasSize(1);
    }

    @Test
    public void testBefore_disabledByFlag_noOp() {
        // -Dpolaris.agent.dubbo.config-center.enabled=false 时,before() 不应改写或新增任何 ConfigCenter
        System.setProperty(
                DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED,
                "false");

        ConfigCenterConfig nacos = new ConfigCenterConfig();
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://1.2.3.4:8848");
        ApplicationModel.getConfigManager().addConfigCenter(nacos);

        interceptor.before(new Object(), null);

        // 原 nacos 完全未变
        Assertions.assertThat(nacos.getProtocol()).isEqualTo("nacos");
        Assertions.assertThat(nacos.getAddress())
                .isEqualTo("nacos://1.2.3.4:8848");
        Collection<ConfigCenterConfig> centers =
                ApplicationModel.getConfigManager().getConfigCenters();
        Assertions.assertThat(centers).hasSize(1);
    }
}
