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

package cn.polarismesh.agent.plugin.dubbo3.interceptor;

import cn.polarismesh.agent.plugin.dubbo3.constants.DubboConstants;
import java.util.Collection;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DubboDeployerInterceptorConfigCenterTest {

    private final DubboDeployerInterceptor interceptor = new DubboDeployerInterceptor();

    private ConfigManager cm() {
        return ApplicationModel.defaultModel().getApplicationConfigManager();
    }

    @Before
    public void setUp() {
        cm().clear();
    }

    @After
    public void tearDown() {
        cm().clear();
        System.clearProperty(DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED);
    }

    @Test
    public void testEmpty_addsPolarisConfigCenter() {
        interceptor.before(new Object(), null);
        Collection<ConfigCenterConfig> all = cm().getConfigCenters();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }

    @Test
    public void testNacos_rewrittenToPolaris() {
        ConfigCenterConfig nacos = new ConfigCenterConfig();
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://127.0.0.1:8848");
        cm().addConfigCenter(nacos);

        interceptor.before(new Object(), null);

        Collection<ConfigCenterConfig> all = cm().getConfigCenters();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }

    @Test
    public void testAlreadyPolaris_noop() {
        ConfigCenterConfig polaris = new ConfigCenterConfig();
        polaris.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        polaris.setAddress("polaris://10.0.0.1:8093");
        cm().addConfigCenter(polaris);

        interceptor.before(new Object(), null);

        Collection<ConfigCenterConfig> all = cm().getConfigCenters();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next()).isSameAs(polaris);
    }

    @Test
    public void testTwoNonPolarisConfigCenters_collapseToSinglePolaris() {
        ConfigCenterConfig zk = new ConfigCenterConfig();
        zk.setId("cc1");
        zk.setProtocol("zookeeper");
        zk.setAddress("zookeeper://127.0.0.1:2181");
        cm().addConfigCenter(zk);

        ConfigCenterConfig nacos = new ConfigCenterConfig();
        nacos.setId("cc2");
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://127.0.0.1:8848");
        cm().addConfigCenter(nacos);

        interceptor.before(new Object(), null);

        Collection<ConfigCenterConfig> all = cm().getConfigCenters();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }

    @Test
    public void testDisabled_skips() {
        System.setProperty(DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED, "false");
        interceptor.before(new Object(), null);
        Assertions.assertThat(cm().getConfigCenters()).isEmpty();
    }
}
