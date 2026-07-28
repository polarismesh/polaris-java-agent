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

package cn.polarismesh.agent.plugin.dubbo3.constants;

/** Dubbo3 Agent 插件常量定义. */
public interface DubboConstants {

    String POLARIS_PROTOCOL = "polaris";

    /** Dubbo3 三路径汇聚的初始化入口类. */
    String DUBBO_DEPLOYER_CLASS =
            "org.apache.dubbo.config.deploy.DefaultApplicationDeployer";

    // ---- 注册中心 ----
    String KEY_DUBBO_REGISTRY_ADDRESS = "dubbo.registry.address";
    String KEY_DUBBO_REGISTRY_PARAMETERS_PREFIX = "dubbo.registry.parameters.";
    String DEFAULT_POLARIS_ADDRESS = "polaris://127.0.0.1:8091";
    String AUTO_REGISTRY_ID_PREFIX_ZERO = "org.apache.dubbo.config.RegistryConfig#0";

    // ---- 配置中心 ----
    String KEY_DUBBO_CONFIG_CENTER_ADDRESS = "dubbo.config-center.address";
    String KEY_DUBBO_CONFIG_CENTER_PARAMETERS_PREFIX = "dubbo.config-center.parameters.";
    String KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED = "polaris.agent.dubbo.config-center.enabled";
    String DEFAULT_CONFIG_CENTER_ADDRESS = "polaris://127.0.0.1:8093";
    String DEFAULT_CONFIG_CENTER_ENABLED = "true";

    // ---- 元数据上报 ----
    String KEY_DUBBO_METADATA_REPORT_ADDRESS = "dubbo.metadata-report.address";
    String KEY_DUBBO_METADATA_REPORT_PARAMETERS_PREFIX = "dubbo.metadata-report.parameters.";
    String KEY_POLARIS_AGENT_DUBBO_METADATA_REPORT_ENABLED = "polaris.agent.dubbo.metadata-report.enabled";
    String DEFAULT_METADATA_REPORT_ADDRESS = "polaris://127.0.0.1:8091";
    String DEFAULT_METADATA_REPORT_ENABLED = "true";
}
