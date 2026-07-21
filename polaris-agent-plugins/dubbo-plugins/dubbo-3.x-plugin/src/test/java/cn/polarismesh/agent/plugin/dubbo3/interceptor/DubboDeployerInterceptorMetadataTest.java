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
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DubboDeployerInterceptorMetadataTest {

    private final DubboDeployerInterceptor interceptor = new DubboDeployerInterceptor();

    private ConfigManager cm() {
        return ApplicationModel.defaultModel().getApplicationConfigManager();
    }

    @Before
    public void setUp() {
        cm().clear();
        // 关闭 config-center 注入,隔离本测试只关注 metadata
        System.setProperty(DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED, "false");
    }

    @After
    public void tearDown() {
        cm().clear();
        System.clearProperty(DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED);
        System.clearProperty(DubboConstants.KEY_POLARIS_AGENT_DUBBO_METADATA_REPORT_ENABLED);
    }

    @Test
    public void testEmpty_addsPolarisMetadata() {
        interceptor.before(new Object(), null);
        Collection<MetadataReportConfig> all = cm().getMetadataConfigs();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }

    @Test
    public void testNacos_rewrittenToPolaris() {
        MetadataReportConfig nacos = new MetadataReportConfig();
        nacos.setAddress("nacos://127.0.0.1:8848");
        cm().addMetadataReport(nacos);

        interceptor.before(new Object(), null);

        Collection<MetadataReportConfig> all = cm().getMetadataConfigs();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
        Assertions.assertThat(all.iterator().next().getAddress())
                .isEqualTo(DubboConstants.DEFAULT_METADATA_REPORT_ADDRESS);
    }

    @Test
    public void testTwoNonPolarisMetadataReports_collapseToSinglePolaris() {
        MetadataReportConfig zk = new MetadataReportConfig();
        zk.setId("mrc1");
        zk.setAddress("zookeeper://127.0.0.1:2181");
        cm().addMetadataReport(zk);

        MetadataReportConfig nacos = new MetadataReportConfig();
        nacos.setId("mrc2");
        nacos.setAddress("nacos://127.0.0.1:8848");
        cm().addMetadataReport(nacos);

        interceptor.before(new Object(), null);

        Collection<MetadataReportConfig> all = cm().getMetadataConfigs();
        Assertions.assertThat(all).hasSize(1);
        Assertions.assertThat(all.iterator().next().getProtocol())
                .isEqualTo(DubboConstants.POLARIS_PROTOCOL);
    }

    @Test
    public void testDisabled_skips() {
        System.setProperty(DubboConstants.KEY_POLARIS_AGENT_DUBBO_METADATA_REPORT_ENABLED, "false");
        interceptor.before(new Object(), null);
        Assertions.assertThat(cm().getMetadataConfigs()).isEmpty();
    }
}
