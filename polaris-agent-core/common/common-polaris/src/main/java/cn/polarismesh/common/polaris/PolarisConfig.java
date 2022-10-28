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

package cn.polarismesh.common.polaris;

import cn.polarismesh.agent.common.config.AgentConfig;
import cn.polarismesh.agent.common.config.InternalConfig;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolarisConfig {

    private static final Logger LOG = LoggerFactory.getLogger(PolarisConfig.class);

    private static final String DEFAULT_NAMESPACE = "default";

    private static final int DEFAULT_TTL = 5;

    private static final int DEFAULT_REFRESH_INTERVAL = 2;

    private final String namespace;

    private final String service;

    private final String registryAddress;

    private final String token;

    private final String agentDir;

    private final int ttl;

    private final int refreshInterval;

    public PolarisConfig() {
        String namespaceStr = System.getProperty(AgentConfig.KEY_NAMESPACE);
        if (null == namespaceStr || namespaceStr.length() == 0) {
            namespaceStr = DEFAULT_NAMESPACE;
        }
        this.namespace = namespaceStr;
        this.service = System.getProperty(AgentConfig.KEY_SERVICE);
        this.token = System.getProperty(AgentConfig.KEY_TOKEN);
        this.registryAddress = System.getProperty(AgentConfig.KEY_REGISTRY);
        this.agentDir = System.getProperty(InternalConfig.INTERNAL_KEY_AGENT_DIR);
        int healthTTL = DEFAULT_TTL;
        String ttlStr = System.getProperty(AgentConfig.KEY_HEALTH_TTL);
        if (null != ttlStr && ttlStr.length() > 0) {
            try {
                healthTTL = Integer.parseInt(ttlStr);
            } catch (Exception e) {
                LOG.info("[Common] fail to convert ttlStr {}", ttlStr, e);
            }
        }
        this.ttl = healthTTL;

        int refreshInterval = DEFAULT_REFRESH_INTERVAL;
        String refreshIntervalStr = System.getProperty(AgentConfig.KEY_REFRESH_INTERVAL);
        if (null != refreshIntervalStr && refreshIntervalStr.length() > 0) {
            try {
                refreshInterval = Integer.parseInt(refreshIntervalStr);
            } catch (Exception e) {
                LOG.info("[Common] fail to convert refreshIntervalStr {}", refreshIntervalStr, e);
            }
        }
        this.refreshInterval = refreshInterval;
        LOG.info("[Common] construct polarisConfig {}", this);

    }

    public String getNamespace() {
        return namespace;
    }

    public String getService() {
        return service;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public String getToken() {
        return token;
    }

    public String getAgentDir() {
        return agentDir;
    }

    public int getTtl() {
        return ttl;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    @Override
    public String toString() {
        return "PolarisConfig{" +
                "namespace='" + namespace + '\'' +
                ", service='" + service + '\'' +
                ", registryAddress='" + registryAddress + '\'' +
                ", token='" + token + '\'' +
                ", agentDir='" + agentDir + '\'' +
                ", ttl=" + ttl +
                ", refreshInterval=" + refreshInterval +
                '}';
    }
}
