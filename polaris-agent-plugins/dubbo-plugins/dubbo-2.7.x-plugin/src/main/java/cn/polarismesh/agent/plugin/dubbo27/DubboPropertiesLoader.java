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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 从 {agent_root}/conf/plugin/dubbo/dubbo-polaris.properties 加载配置属性.
 *
 * <p>文件不存在或读取失败时返回空 Properties，并记录日志，不抛异常。</p>
 */
public final class DubboPropertiesLoader {

    private static final Logger LOGGER =
            Logger.getLogger(DubboPropertiesLoader.class.getName());

    /** JVM 系统属性名，值为 agent 根目录路径。 */
    public static final String AGENT_CONF_PATH_PROPERTY =
            "__agent_conf_path__";

    /** 相对于 agent 根目录的配置文件路径。 */
    static final String CONFIG_FILE_PATH =
            "conf/plugin/dubbo/dubbo-polaris.properties";

    /** Dubbo 注册中心扩展参数 JVM 系统属性前缀。 */
    static final String REGISTRY_PARAMETERS_PREFIX =
            "dubbo.registry.parameters.";

    private DubboPropertiesLoader() {
    }

    /**
     * 加载配置文件，返回解析后的 Properties.
     * 文件不存在或 IO 异常时返回空 Properties。
     *
     * @return 解析后的 Properties，从不为 null
     */
    public static Properties loadProperties() {
        Properties props = new Properties();
        String agentConfPath =
                System.getProperty(AGENT_CONF_PATH_PROPERTY);
        if (agentConfPath == null || agentConfPath.trim().isEmpty()) {
            LOGGER.warning("System property " + AGENT_CONF_PATH_PROPERTY
                    + " is not set, skipping Dubbo config file loading");
            return props;
        }
        File configFile = new File(agentConfPath, CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            LOGGER.info("Dubbo config file not found, skipping: "
                    + configFile.getAbsolutePath());
            return props;
        }
        try (InputStream in = new FileInputStream(configFile)) {
            props.load(in);
        } catch (IOException e) {
            LOGGER.warning("Failed to read Dubbo config file: "
                    + configFile.getAbsolutePath()
                    + ", cause: " + e.getMessage());
        }
        return props;
    }

    /**
     * 从 JVM 系统属性读取注册中心扩展参数.
     *
     * <p>读取所有 {@code -Ddubbo.registry.parameters.*} 形式的系统属性，
     * 去掉 {@code dubbo.registry.parameters.} 前缀后返回。</p>
     *
     * @return 注册中心扩展参数 Map，不为 null
     */
    public static Map<String, String> loadSystemRegistryParameters() {
        Properties props = System.getProperties();
        Map<String, String> params = new HashMap<String, String>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(REGISTRY_PARAMETERS_PREFIX)) {
                params.put(key.substring(REGISTRY_PARAMETERS_PREFIX.length()),
                        props.getProperty(key));
            }
        }
        return params;
    }
}
