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
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DubboDeployerInterceptorRegistryTest {

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
        System.clearProperty(DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS);
    }

    @Test
    public void testEmptyRegistries_addsPolaris() {
        interceptor.after(new Object(), null, null, null);
        Collection<RegistryConfig> all = cm().getRegistries();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(all.iterator().next().getAddress())
                .isEqualTo(DubboConstants.DEFAULT_POLARIS_ADDRESS);
    }

    @Test
    public void testZookeeperRegistry_rewrittenToPolaris() {
        RegistryConfig zk = new RegistryConfig();
        zk.setProtocol("zookeeper");
        zk.setAddress("zookeeper://127.0.0.1:2181");
        cm().addRegistry(zk);

        interceptor.after(new Object(), null, null, null);

        Assertions.assertThat(zk.getProtocol()).isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(zk.getAddress()).isEqualTo(DubboConstants.DEFAULT_POLARIS_ADDRESS);
    }

    @Test
    public void testAlreadyPolaris_noop() {
        RegistryConfig polaris = new RegistryConfig();
        polaris.setId("user-polaris");
        polaris.setProtocol(DubboConstants.POLARIS_PROTOCOL);
        polaris.setAddress("polaris://10.0.0.1:8091");
        cm().addRegistry(polaris);

        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> all = cm().getRegistries();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next()).isSameAs(polaris);
        Assertions.assertThat(polaris.getAddress()).isEqualTo("polaris://10.0.0.1:8091");
    }

    @Test
    public void testTwoNonPolarisRegistries_collapseToSinglePolaris() {
        RegistryConfig zk = new RegistryConfig();
        zk.setId("reg1");
        zk.setProtocol("zookeeper");
        zk.setAddress("zookeeper://127.0.0.1:2181");
        cm().addRegistry(zk);

        RegistryConfig nacos = new RegistryConfig();
        nacos.setId("reg2");
        nacos.setProtocol("nacos");
        nacos.setAddress("nacos://127.0.0.1:8848");
        cm().addRegistry(nacos);

        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> all = cm().getRegistries();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }

    @Test
    public void testIdempotentAfterTwice() {
        RegistryConfig zk = new RegistryConfig();
        zk.setProtocol("nacos");
        zk.setAddress("nacos://127.0.0.1:8848");
        cm().addRegistry(zk);

        interceptor.after(new Object(), null, null, null);
        interceptor.after(new Object(), null, null, null);

        Collection<RegistryConfig> all = cm().getRegistries();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }
}
