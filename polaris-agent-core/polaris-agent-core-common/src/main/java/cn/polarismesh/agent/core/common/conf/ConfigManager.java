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

package cn.polarismesh.agent.core.common.conf;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.common.utils.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {

    public static final String KEY_PLUGIN_ENABLE = "plugins.enable";

    private static final String CONFIG_FILE_NAME = "polaris-agent.config";

    private static final String[] KEYS = new String[]{KEY_PLUGIN_ENABLE};

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ConfigManager.class.getCanonicalName());

    public static final ConfigManager INSTANCE = new ConfigManager();

    private final Properties properties = new Properties();

    private ConfigManager() {

    }

    public void initConfig(String agentBootDir) {
        String configFullPath = agentBootDir + File.separator + "conf" + File.separator + CONFIG_FILE_NAME;
        logger.info(String.format("[BootConfig] start to init config by path %s", configFullPath));
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(configFullPath));
            properties.load(bufferedReader);
        } catch (IOException e) {
            logger.warn(
                    String.format("[BootConfig] fail to load config file %s, err: %s", configFullPath, e.getMessage()));
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String key : KEYS) {
            String value = System.getProperty(key);
            if (StringUtils.hasText(value)) {
                properties.setProperty(key, value);
            }
        }
        logger.info(String.format("[BootConfig] agent config loaded, values %s", properties.toString()));
    }

    public String getConfigValue(String key) {
        return properties.getProperty(key);
    }

}
