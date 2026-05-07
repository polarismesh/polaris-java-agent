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

import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import cn.polarismesh.agent.plugin.dubbo27.constants.DubboConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * DubboMainPlugin 单元测试.
 */
@RunWith(MockitoJUnitRunner.class)
public class DubboMainPluginTest {

    @Mock
    private PluginContext mockContext;

    @Mock
    private TransformOperations mockTransformOps;

    private DubboMainPlugin plugin;

    @Before
    public void setUp() {
        this.plugin = new DubboMainPlugin();
        Mockito.when(this.mockContext.getAgentDirPath())
                .thenReturn("/tmp/fake-agent-dir");
        Mockito.when(this.mockContext.getTransformOperations())
                .thenReturn(this.mockTransformOps);
    }

    @Test
    public void testInitRegistersDubboBootstrapTransform() {
        // 验证 init 方法注册了 DubboBootstrap 的 transform
        this.plugin.init(this.mockContext);

        Mockito.verify(this.mockTransformOps).transform(
                Mockito.eq(DubboConstants.DUBBO_BOOTSTRAP_CLASS),
                Mockito.any(Class.class));
        Mockito.verify(this.mockTransformOps, Mockito.times(1))
                .transform(Mockito.anyString(),
                        Mockito.any(Class.class));
    }
}
