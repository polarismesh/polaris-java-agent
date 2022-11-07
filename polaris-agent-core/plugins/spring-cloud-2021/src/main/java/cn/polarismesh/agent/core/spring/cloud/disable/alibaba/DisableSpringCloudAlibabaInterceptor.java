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

package cn.polarismesh.agent.core.spring.cloud.disable.alibaba;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * {@link org.springframework.boot.util.Instantiator#instantiate(Stream)}
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class DisableSpringCloudAlibabaInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisableSpringCloudAlibabaInterceptor.class);

	@Override
	public void before(Object target, Object[] args) {
		ApplicationEnvironmentPreparedEvent event = (ApplicationEnvironmentPreparedEvent) args[0];

		List<EnvironmentPostProcessor> external = new ArrayList<>();
		// 插入禁止 spring cloud alibaba 的一切能力
		external.add(new DisableSpringCloudAlibabaAbility());

		for (EnvironmentPostProcessor processor : external) {
			processor.postProcessEnvironment(event.getEnvironment(), event.getSpringApplication());
		}
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
	}

	private static class DisableSpringCloudAlibabaAbility implements EnvironmentPostProcessor {

		@Override
		public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
			disableSentinelAbility(environment);
		}

		private void disableSentinelAbility(ConfigurableEnvironment environment) {

			String disableSentinel = "__disable__sentinel__";

			Properties properties = new Properties();
			properties.setProperty("spring.cloud.sentinel.enabled", "false");
//			properties.setProperty("spring.cloud.nacos.discovery.enabled", "false");
			properties.setProperty("spring.cloud.nacos.config.enabled", "false");

			// 设置 spring.cloud.sentinel.enabled 为 false
			environment.getPropertySources().addFirst(new PropertiesPropertySource(disableSentinel, properties));

			LOGGER.info("[PolarisAgent] disable spring cloud alibaba all ability");
		}
	}

}
