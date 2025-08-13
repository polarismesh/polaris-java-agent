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

package cn.polarismesh.agent.examples.alibaba.cloud.cloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentChangeEventListener {
	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentChangeEventListener.class);

	private final Environment environment;

	public EnvironmentChangeEventListener(Environment environment) {
		this.environment = environment;
	}

	@EventListener
	public void handleConfigChange(EnvironmentChangeEvent event) {
		if (event == null) {
			LOG.warn("Received null environment change event");
			return;
		}

		if (event.getKeys().isEmpty()) {
			LOG.warn("Received empty keys in environment change event. Event details: {}", event);
			LOG.info("Current environment properties, Active: {}, Default:{}", environment.getActiveProfiles(),
					environment.getDefaultProfiles());
			return;
		}

		StringBuilder changes = new StringBuilder();
		changes.append("Environment configuration changes:\n");

		event.getKeys().forEach(key -> {
			String value = environment.getProperty(key);
			changes.append(String.format("  %s = %s%n", key, value));
		});

		LOG.info(changes.toString());
	}
}

