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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * DubboPropertiesLoader 单元测试.
 */
public class DubboPropertiesLoaderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        System.clearProperty(DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY);
    }

    @After
    public void tearDown() {
        System.clearProperty(DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY);
    }

    @Test
    public void testLoadProperties_agentPathNotSet() {
        // 未设置 __agent_conf_path__ 时，返回空 Properties
        Properties props = DubboPropertiesLoader.loadProperties();
        Assertions.assertThat(props).isEmpty();
    }

    @Test
    public void testLoadProperties_fileNotFound() {
        // __agent_conf_path__ 指向不存在路径，返回空 Properties
        System.setProperty(DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY,
                "/nonexistent/path/that/does/not/exist");
        Properties props = DubboPropertiesLoader.loadProperties();
        Assertions.assertThat(props).isEmpty();
    }

    @Test
    public void testLoadProperties_validFile() throws IOException {
        // 临时文件包含有效属性，正确解析
        File confDir = tempFolder.newFolder("conf", "plugin", "dubbo");
        File propsFile = new File(confDir, "dubbo-polaris.properties");
        try (FileWriter writer = new FileWriter(propsFile)) {
            writer.write("dubbo.registry.address=polaris://10.0.0.1:8091\n");
            writer.write("dubbo.registry.parameters.polaris_nacos_enabled=true\n");
        }
        System.setProperty(DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY,
                tempFolder.getRoot().getAbsolutePath());

        Properties props = DubboPropertiesLoader.loadProperties();

        Assertions.assertThat(props.getProperty("dubbo.registry.address"))
                .isEqualTo("polaris://10.0.0.1:8091");
        Assertions.assertThat(
                props.getProperty("dubbo.registry.parameters.polaris_nacos_enabled"))
                .isEqualTo("true");
    }

    @Test
    public void testLoadProperties_ioException() throws IOException {
        // 文件路径存在但实际是目录，触发 IO 异常，返回空 Properties
        File confDir = tempFolder.newFolder("conf", "plugin", "dubbo");
        File propsFile = new File(confDir, "dubbo-polaris.properties");
        propsFile.mkdir();
        System.setProperty(DubboPropertiesLoader.AGENT_CONF_PATH_PROPERTY,
                tempFolder.getRoot().getAbsolutePath());

        Properties props = DubboPropertiesLoader.loadProperties();

        Assertions.assertThat(props).isEmpty();
    }

    @Test
    public void testLoadSystemParametersByPrefix_collectsAndStripsPrefix() {
        // 通用前缀剥离 - 任意前缀都应正确收集并去掉前缀
        System.setProperty("foo.bar.alpha", "1");
        System.setProperty("foo.bar.beta", "2");
        System.setProperty("foo.bar.", "should-be-empty-key");
        System.setProperty("other.unrelated", "ignored");
        try {
            Map<String, String> result =
                    DubboPropertiesLoader.loadSystemParametersByPrefix("foo.bar.");

            Assertions.assertThat(result)
                    .containsEntry("alpha", "1")
                    .containsEntry("beta", "2")
                    .doesNotContainKey("unrelated");
        } finally {
            System.clearProperty("foo.bar.alpha");
            System.clearProperty("foo.bar.beta");
            System.clearProperty("foo.bar.");
            System.clearProperty("other.unrelated");
        }
    }
}
