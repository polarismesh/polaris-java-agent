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
}
