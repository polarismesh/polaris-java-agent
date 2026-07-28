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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public final class DubboPropertiesLoader {

    private static final Logger LOGGER =
            Logger.getLogger(DubboPropertiesLoader.class.getName());

    public static final String AGENT_CONF_PATH_PROPERTY = "__agent_conf_path__";

    static final String CONFIG_FILE_PATH =
            "conf/plugin/dubbo/dubbo-polaris.properties";

    private DubboPropertiesLoader() {
    }

    public static Properties loadProperties() {
        Properties props = new Properties();
        String agentConfPath = System.getProperty(AGENT_CONF_PATH_PROPERTY);
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
                    + configFile.getAbsolutePath() + ", cause: " + e.getMessage());
        }
        return props;
    }

    public static Map<String, String> loadSystemParametersByPrefix(String prefix) {
        Properties props = System.getProperties();
        Map<String, String> params = new HashMap<String, String>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                params.put(key.substring(prefix.length()), props.getProperty(key));
            }
        }
        return params;
    }
}
