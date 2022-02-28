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
    String KEY_TOKEN = "agent.application.token";

    /**
     * polaris registry address
     */
    String KEY_REGISTRY = "agent.polaris.registry";

    /**
     * polaris healthcheck ttl seconds
     */
    String KEY_HEALTH_TTL = "agent.application.healthcheck.ttl";

    /**
     * application version
     */
    String KEY_VERSION = "agent.application.version";

    /**
     * agent exec dir
     */
    String INTERNAL_KEY_AGENT_DIR = "internal.agent.dir";
}
