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

import java.util.HashSet;
import java.util.Set;

import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.config.BeanDefinition;

public class RegisterBeanInterceptor implements Interceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegisterBeanInterceptor.class);

	private final Set<String> primaryBeanNames = new HashSet<>();

	public RegisterBeanInterceptor() {
		primaryBeanNames.add("polarisAutoServiceRegistration");
		primaryBeanNames.add("polarisRegistration");
		primaryBeanNames.add("polarisServiceRegistry");
	}

	@Override
	public void before(Object target, Object[] args) {
		String beanName = (String) args[0];
		if (!primaryBeanNames.contains(beanName)) {
			return;
		}
		BeanDefinition beanDefinition = (BeanDefinition) args[1];
		beanDefinition.setPrimary(true);
		LOGGER.info("[PolarisJavaAgent] bean {} has been made primary", beanName);
	}
}
