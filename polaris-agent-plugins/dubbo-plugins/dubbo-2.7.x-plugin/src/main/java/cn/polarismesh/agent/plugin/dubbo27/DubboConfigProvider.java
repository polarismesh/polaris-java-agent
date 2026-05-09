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
 * Dubbo 插件配置提供者，负责获取 Polaris 服务端地址和注册中心扩展参数.
 *
 * <p>地址优先级：JVM 系统属性 {@code dubbo.registry.address}
 * &gt; 本地配置文件 {@code conf/plugin/dubbo/dubbo-polaris.properties}
 * &gt; 默认值 {@code polaris://127.0.0.1:8091}.</p>
 */
public final class DubboConfigProvider {

    private DubboConfigProvider() {
    }

    /**
     * 获取 Polaris 服务端地址.
     *
     * <p>依次尝试：JVM 系统属性、本地配置文件、硬编码默认值。</p>
     *
     * @return Polaris 服务端地址，不为 null
     */
    public static String getPolarisServerAddress() {
        String address = System.getProperty(
                DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS);
        if (address == null || address.trim().isEmpty()) {
            address = DubboPropertiesLoader.loadProperties()
                    .getProperty(
                            DubboConstants.KEY_DUBBO_REGISTRY_ADDRESS);
        }
        if (address == null || address.trim().isEmpty()) {
            return DubboConstants.DEFAULT_POLARIS_ADDRESS;
        }
        return address.trim();
    }

    /**
     * 获取注册中心扩展参数.
     *
     * <p>从配置文件读取所有 {@code dubbo.registry.parameters.*} 键，
     * 去掉前缀后返回。文件不存在或无匹配项时返回空 Map。</p>
     *
     * @return 注册中心扩展参数 Map，不为 null
     */
    public static Map<String, String> getRegistryParameters() {
        Properties props = DubboPropertiesLoader.loadProperties();
        String prefix = "dubbo.registry.parameters.";
        Map<String, String> params = new HashMap<String, String>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                params.put(key.substring(prefix.length()),
                        props.getProperty(key));
            }
        }
        return params;
    }
}
