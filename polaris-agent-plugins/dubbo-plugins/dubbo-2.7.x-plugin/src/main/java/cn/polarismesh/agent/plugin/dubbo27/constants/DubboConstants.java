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

package cn.polarismesh.agent.plugin.dubbo27.constants;

/**
 * Dubbo Agent 插件常量定义.
 */
public interface DubboConstants {

    /** Polaris 注册中心协议名. */
    String POLARIS_PROTOCOL = "polaris";

    /** Dubbo DubboBootstrap 全限定类名. */
    String DUBBO_BOOTSTRAP_CLASS =
            "org.apache.dubbo.config.bootstrap.DubboBootstrap";

    /** Polaris 默认服务端地址. */
    String DEFAULT_POLARIS_ADDRESS = "polaris://127.0.0.1:8091";

    /** 系统属性键: Dubbo 注册中心地址，同时作为 Polaris 地址来源. */
    String KEY_DUBBO_REGISTRY_ADDRESS = "dubbo.registry.address";

    String KEY_DUBBO_REGISTRY_PARAMETERS_PREFIX = "dubbo.registry.parameters.";

    /** 系统属性 / properties 键: config-center 地址。 */
    String KEY_DUBBO_CONFIG_CENTER_ADDRESS = "dubbo.config-center.address";

    /** 系统属性 / properties 前缀: config-center 扩展参数。 */
    String KEY_DUBBO_CONFIG_CENTER_PARAMETERS_PREFIX = "dubbo.config-center.parameters.";

    /** 系统属性 / properties 键: config-center 注入开关。默认 true。 */
    String KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED = "polaris.agent.dubbo.config-center.enabled";

    /** Polaris config-center 默认地址。注意端口 8093(配置中心) ≠ 8091(服务发现). */
    String DEFAULT_CONFIG_CENTER_ADDRESS = "polaris://127.0.0.1:8093";

    /** Polaris config-center 注入开关默认值。 */
    String DEFAULT_CONFIG_CENTER_ENABLED = "true";

    /**
     * Dubbo 在用户未显式 setId 时按声明顺序自动赋的第 0 个 RegistryConfig 的 id。
     * 用于 single-registry 改写路径定位"首位",比 HashMap iter 顺序稳定。
     *
     * <p>实测基于 Dubbo 2.7.23。当 fallback 路径(找不到此 id)走到时,
     * 退回 iterator().next(),行为退化为 HashMap 顺序——同 JVM 可重现,
     * 跨 JVM/JDK 可能变化。</p>
     */
    String AUTO_REGISTRY_ID_PREFIX_ZERO = "org.apache.dubbo.config.RegistryConfig#0";

    String AUTO_CONFIG_CENTER_ID_PREFIX_ZERO = "org.apache.dubbo.config.spring.ConfigCenterBean#0";

}
