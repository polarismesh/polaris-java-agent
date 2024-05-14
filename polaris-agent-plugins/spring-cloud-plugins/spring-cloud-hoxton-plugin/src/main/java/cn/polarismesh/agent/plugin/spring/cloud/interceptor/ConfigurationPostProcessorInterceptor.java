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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import java.util.List;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.spring.cloud.common.PropertiesProvider;
import cn.polarismesh.agent.plugin.spring.cloud.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

public class ConfigurationPostProcessorInterceptor implements Interceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationPostProcessorInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {
		ConfigurableEnvironment environment = (ConfigurableEnvironment) ReflectionUtils.getObjectByFieldName(target, "environment");
		if (!Utils.checkSpringApplicationNameExists(environment)) {
			LOGGER.warn("[PolarisJavaAgent] skip inject polaris java agent configuration for no spring application name");
			return;
		}
		List<PropertiesPropertySource> propertySources = PropertiesProvider.loadPropertiesSource();
		MutablePropertySources mutablePropertySources = environment.getPropertySources();
		if (mutablePropertySources.contains(propertySources.get(0).getName())) {
			return;
		}
		for (PropertiesPropertySource propertiesPropertySource : propertySources) {
			LOGGER.info("[PolarisJavaAgent] start to add propertiesPropertySource {}", propertiesPropertySource.getName());
			environment.getPropertySources().addLast(propertiesPropertySource);
		}
		LOGGER.info("[PolarisJavaAgent] successfully injected agent properties into environment, size is " + propertySources.size());
	}

}
