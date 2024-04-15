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

package cn.polarismesh.agent.plugin.spring.cloud.config;

import java.util.function.Supplier;

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.base.AbstractContextHandler;
import com.tencent.cloud.polaris.config.PolarisConfigAutoConfiguration;
import com.tencent.cloud.polaris.config.adapter.PolarisConfigFileLocator;
import com.tencent.cloud.polaris.config.adapter.PolarisPropertySourceManager;
import com.tencent.cloud.polaris.config.config.PolarisConfigProperties;
import com.tencent.cloud.polaris.config.config.PolarisCryptoConfigProperties;
import com.tencent.polaris.configuration.api.core.ConfigFileService;
import com.tencent.polaris.configuration.factory.ConfigFileServiceFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

public class ConfigHandler extends AbstractContextHandler {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (!Holder.getPolarisConfigProperties().isEnabled()) {
			return;
		}
		registerBean(applicationContext, "polarisProperties", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisConfigProperties.class, new Supplier<PolarisConfigProperties>() {
						@Override
						public PolarisConfigProperties get() {
							return Holder.getPolarisConfigProperties();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisCryptoConfigProperties", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisCryptoConfigProperties.class, new Supplier<PolarisCryptoConfigProperties>() {
						@Override
						public PolarisCryptoConfigProperties get() {
							return Holder.getPolarisCryptoConfigProperties();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisPropertySourceManager", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisPropertySourceManager.class, new Supplier<PolarisPropertySourceManager>() {
						@Override
						public PolarisPropertySourceManager get() {
							return new PolarisPropertySourceManager();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "configFileService", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(ConfigFileService.class, new Supplier<ConfigFileService>() {
						@Override
						public ConfigFileService get() {
							return ConfigFileServiceFactory.createConfigFileService(Holder.getContextManager().getSDKContext());
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisConfigFileLocator", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisConfigFileLocator.class, new Supplier<PolarisConfigFileLocator>() {
						@Override
						public PolarisConfigFileLocator get() {
							ConfigFileService configFileService = cfgCtx.getBean("configFileService", ConfigFileService.class);
							PolarisPropertySourceManager polarisPropertySourceManager = cfgCtx.getBean("polarisPropertySourceManager", PolarisPropertySourceManager.class);
							Environment environment = cfgCtx.getBean("environment", Environment.class);
							return new PolarisConfigFileLocator(Holder.getPolarisConfigProperties(),
									Holder.getPolarisContextProperties(), configFileService,
									polarisPropertySourceManager, environment);
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisConfigAutoConfiguration",  (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisConfigAutoConfiguration.class).getBeanDefinition());
		});
	}

}
