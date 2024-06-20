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

package cn.polarismesh.agent.plugin.spring.cloud.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class PropertiesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesProvider.class);

    private static final Map<String, List<PropertiesPropertySource>> LOADED_RESOURCES = new ConcurrentHashMap<>();

    private static final String CONFIG_FILE_NAME = "default-plugin.conf";

    /**
     * load the properties source from default application.yaml
     *
     * @return propertySources
     */
    public static List<PropertiesPropertySource> loadPropertiesSource() {
        return LOADED_RESOURCES.computeIfAbsent(CONFIG_FILE_NAME, fileName -> {
            InputStream stream = PropertiesProvider.class.getClassLoader().getResourceAsStream(fileName);
            Properties defaultProperties = new Properties();
            try {
                defaultProperties.load(stream);
            } catch (IOException e) {
                throw new IllegalStateException("fail to load file " + fileName, e);
            }
            List<PropertiesPropertySource> propertySources = new ArrayList<>();
            String configPath = Paths.get(System.getProperty(Constant.AGENT_CONF_PATH), "conf").toString();
            LOGGER.info("load property sources from config path " + configPath);
            Properties properties = new Properties();
            String confPath = Paths.get(configPath, "plugin", "spring-cloud", "application.properties").toString();
            String cmdVal = System.getProperty("polaris.agent.user.application.conf");
            if (null != cmdVal && !cmdVal.isEmpty()) {
                confPath = cmdVal;
            }
            try {
                properties.load(Files.newInputStream(Paths.get(confPath).toFile().toPath()));
            } catch (IOException e) {
                throw new IllegalStateException("fail to load config from " + configPath, e);
            }
            properties.setProperty("spring.cloud.nacos.config.enabled", "false");
            properties.setProperty("spring.cloud.nacos.discovery.enabled", "false");
            propertySources.add(new PropertiesPropertySource("__polaris_agent_spring_cloud_tencent__", properties));
            propertySources.add(new PropertiesPropertySource("__default_polaris_agent_spring_cloud_tencent__", defaultProperties));
            return propertySources;
        });
    }
}
