/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
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

package cn.polarismesh.agent.common.config;

public interface AgentConfig {

    /**
     * namespace to register service
     */
    String KEY_NAMESPACE = "agent.application.namespace";

    /**
     * application name
     */
    String KEY_SERVICE = "agent.application.name";

    /**
     * token to validata
     */
    String KEY_TOKEN = "agent.polaris.token";

    /**
     * polaris healthcheck ttl seconds
     */
    String KEY_HEALTH_TTL = "agent.polaris.instance.health.ttl";

    /**
     * polaris registry center address
     */
    String KEY_REGISTRY_ADDRESS = "agent.polaris.registry.address";

    /**
     * polaris config center address
     */
    String KEY_CONFIG_ADDRESS = "agent.polaris.config.address";

    /**
     * enable springcloud register injection
     */
    String KEY_PLUGIN_SPRINGCLOUD_REGISTER_ENABLE = "agent.plugin.springcloud.register.enable";

    /**
     * enable springcloud multi register injection
     */
    String KEY_PLUGIN_SPRINGCLOUD_MULTI_REGISTER_ENABLE = "agent.plugin.springcloud.multi.register.enable";

    /**
     * enable springcloud discovery injection
     */
    String KEY_PLUGIN_SPRINGCLOUD_DISCOVERY_ENABLE = "agent.plugin.springcloud.discovery.enable";

    /**
     * enable springcloud stainer injection
     */
    String KEY_PLUGIN_SPRINGCLOUD_STAINER_ENABLE = "agent.plugin.springcloud.stainer.enable";

    /**
     * enable springcloud router injection
     */
    String KEY_PLUGIN_SPRINGCLOUD_ROUTER_ENABLE = "agent.plugin.springcloud.router.enable";

    /**
     * enable springcloud limiter injection
     */
    String KEY_PLUGIN_SPRINGCLOUD_LIMITER_ENABLE = "agent.plugin.springcloud.limiter.enable";

    /**
     * all the reference keys
     */
    String[] KEYS = {KEY_NAMESPACE, KEY_SERVICE, KEY_TOKEN, KEY_REGISTRY_ADDRESS, KEY_CONFIG_ADDRESS, KEY_HEALTH_TTL,
            KEY_PLUGIN_SPRINGCLOUD_REGISTER_ENABLE, KEY_PLUGIN_SPRINGCLOUD_MULTI_REGISTER_ENABLE,
            KEY_PLUGIN_SPRINGCLOUD_DISCOVERY_ENABLE, KEY_PLUGIN_SPRINGCLOUD_STAINER_ENABLE, KEY_PLUGIN_SPRINGCLOUD_ROUTER_ENABLE,
            KEY_PLUGIN_SPRINGCLOUD_LIMITER_ENABLE};

}
