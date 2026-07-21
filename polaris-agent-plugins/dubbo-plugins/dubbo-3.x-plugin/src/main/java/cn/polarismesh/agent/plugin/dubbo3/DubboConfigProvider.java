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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class DubboConfigProvider {

    private DubboConfigProvider() {
    }

    public static String getPolarisServerAddress() {
        return resolveString(DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS,
                DubboConstants.DEFAULT_POLARIS_ADDRESS);
    }

    public static Map<String, String> getRegistryParameters() {
        return collectParametersByPrefix(DubboConstants.KEY_DUBBO_REGISTRY_PARAMETERS_PREFIX);
    }

    public static String getConfigCenterAddress() {
        return resolveString(DubboConstants.KEY_DUBBO_CONFIG_CENTER_ADDRESS,
                DubboConstants.DEFAULT_CONFIG_CENTER_ADDRESS);
    }

    public static Map<String, String> getConfigCenterParameters() {
        return collectParametersByPrefix(DubboConstants.KEY_DUBBO_CONFIG_CENTER_PARAMETERS_PREFIX);
    }

    public static boolean isConfigCenterEnabled() {
        return Boolean.parseBoolean(resolveString(
                DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED,
                DubboConstants.DEFAULT_CONFIG_CENTER_ENABLED));
    }

    public static String getMetadataReportAddress() {
        return resolveString(DubboConstants.KEY_DUBBO_METADATA_REPORT_ADDRESS,
                DubboConstants.DEFAULT_METADATA_REPORT_ADDRESS);
    }

    public static Map<String, String> getMetadataReportParameters() {
        return collectParametersByPrefix(DubboConstants.KEY_DUBBO_METADATA_REPORT_PARAMETERS_PREFIX);
    }

    public static boolean isMetadataReportEnabled() {
        return Boolean.parseBoolean(resolveString(
                DubboConstants.KEY_POLARIS_AGENT_DUBBO_METADATA_REPORT_ENABLED,
                DubboConstants.DEFAULT_METADATA_REPORT_ENABLED));
    }

    private static String resolveString(String key, String defaultValue) {
        String value = System.getProperties().getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            value = DubboPropertiesLoader.loadProperties().getProperty(key);
        }
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static Map<String, String> collectParametersByPrefix(String prefix) {
        Properties props = DubboPropertiesLoader.loadProperties();
        Map<String, String> params = new HashMap<String, String>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                params.put(key.substring(prefix.length()), props.getProperty(key));
            }
        }
        params.putAll(DubboPropertiesLoader.loadSystemParametersByPrefix(prefix));
        return params;
    }
}
