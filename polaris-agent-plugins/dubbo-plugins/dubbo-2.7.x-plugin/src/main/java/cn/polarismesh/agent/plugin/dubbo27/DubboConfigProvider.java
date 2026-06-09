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

import cn.polarismesh.agent.plugin.dubbo27.constants.DubboConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Dubbo 插件配置提供者，负责获取 Polaris 服务端地址、注册中心扩展参数,
 * 以及 config-center 相关配置。
 *
 * <p>地址优先级 (标量):
 *   JVM 系统属性 &gt; 本地配置文件 &gt; 默认值。</p>
 * <p>扩展参数 (前缀 Map):
 *   文件按前缀收集,sysprop 按前缀收集后 putAll 覆盖。</p>
 */
public final class DubboConfigProvider {

    private DubboConfigProvider() {
    }

    // === 已有 API,行为不变 ===

    public static String getPolarisServerAddress() {
        return resolveString(
                DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS,
                DubboConstants.DEFAULT_POLARIS_ADDRESS);
    }

    public static Map<String, String> getRegistryParameters() {
        return collectParametersByPrefix(
                DubboConstants.KEY_DUBBO_REGISTRY_PARAMETERS_PREFIX);
    }

    // === 新增: config-center API ===

    /**
     * 获取 Polaris config-center 地址。
     * 优先级: sysprop > properties 文件 > 默认值 (polaris://127.0.0.1:8093)。
     */
    public static String getConfigCenterAddress() {
        return resolveString(
                DubboConstants.KEY_DUBBO_CONFIG_CENTER_ADDRESS,
                DubboConstants.DEFAULT_CONFIG_CENTER_ADDRESS);
    }

    /**
     * 获取 config-center 扩展参数 (含 token、加密开关等)。
     */
    public static Map<String, String> getConfigCenterParameters() {
        return collectParametersByPrefix(
                DubboConstants.KEY_DUBBO_CONFIG_CENTER_PARAMETERS_PREFIX);
    }

    /**
     * 获取 config-center 注入开关。默认 true。
     */
    public static boolean isConfigCenterEnabled() {
        String value = resolveString(
                DubboConstants.KEY_POLARIS_AGENT_DUBBO_CONFIG_CENTER_ENABLED,
                DubboConstants.DEFAULT_CONFIG_CENTER_ENABLED);
        return Boolean.parseBoolean(value);
    }

    // === 私有公共逻辑 ===

    /** sysprop > properties 文件 > 默认值,空字符串视为未配。 */
    private static String resolveString(String key, String defaultValue) {
        Properties props = System.getProperties();
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            value = DubboPropertiesLoader.loadProperties().getProperty(key);
        }
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    /** properties 文件按前缀收集 + sysprop 按前缀收集,sysprop 覆盖文件。 */
    private static Map<String, String> collectParametersByPrefix(String prefix) {
        Properties props = DubboPropertiesLoader.loadProperties();
        Map<String, String> params = new HashMap<String, String>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                params.put(key.substring(prefix.length()),
                        props.getProperty(key));
            }
        }
        params.putAll(
                DubboPropertiesLoader.loadSystemParametersByPrefix(prefix));
        return params;
    }
}
