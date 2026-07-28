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

package cn.polarismesh.agent.plugin.dubbo3;

import cn.polarismesh.agent.plugin.dubbo3.constants.DubboConstants;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;

public class DubboConfigProviderTest {

    @After
    public void tearDown() {
        System.clearProperty(DubboConstants.KEY_DUBBO_METADATA_REPORT_ADDRESS);
        System.clearProperty(DubboConstants.KEY_POLARIS_AGENT_DUBBO_METADATA_REPORT_ENABLED);
        System.clearProperty(DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY);
    }

    @Test
    public void testMetadataReportDefaults() {
        Assertions.assertThat(DubboConfigProvider.getMetadataReportAddress())
                .isEqualTo(DubboConstants.DEFAULT_METADATA_REPORT_ADDRESS);
        Assertions.assertThat(DubboConfigProvider.isMetadataReportEnabled()).isTrue();
    }

    @Test
    public void testMetadataReportSyspropOverride() {
        System.setProperty(DubboConstants.KEY_DUBBO_METADATA_REPORT_ADDRESS,
                "polaris://1.2.3.4:8091");
        System.setProperty(DubboConstants.KEY_POLARIS_AGENT_DUBBO_METADATA_REPORT_ENABLED,
                "false");
        Assertions.assertThat(DubboConfigProvider.getMetadataReportAddress())
                .isEqualTo("polaris://1.2.3.4:8091");
        Assertions.assertThat(DubboConfigProvider.isMetadataReportEnabled()).isFalse();
    }
}
