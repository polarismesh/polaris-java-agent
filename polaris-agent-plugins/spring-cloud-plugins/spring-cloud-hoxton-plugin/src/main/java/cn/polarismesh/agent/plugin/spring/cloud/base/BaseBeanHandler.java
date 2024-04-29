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

package cn.polarismesh.agent.plugin.spring.cloud.base;

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import com.tencent.cloud.common.metadata.StaticMetadataManager;
import com.tencent.cloud.polaris.context.PolarisSDKContextManager;
import com.tencent.cloud.polaris.context.ServiceRuleManager;
import com.tencent.cloud.polaris.context.config.PolarisContextPostConfiguration;
import com.tencent.cloud.polaris.context.config.PolarisContextProperties;
import com.tencent.cloud.polaris.loadbalancer.config.PolarisLoadBalancerAutoConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.Supplier;

public class BaseBeanHandler extends AbstractContextHandler {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		registerPolarisSDKContextManager(applicationContext);
		registerBean(applicationContext, "staticMetadataManager", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(StaticMetadataManager.class, new Supplier<StaticMetadataManager>() {
						@Override
						public StaticMetadataManager get() {
							return Holder.getStaticMetadataManager();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisContextPostConfiguration", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(
					PolarisContextPostConfiguration.class).getBeanDefinition());
		});
		registerBean(applicationContext, "serviceRuleManager", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(ServiceRuleManager.class, new Supplier<ServiceRuleManager>() {
						@Override
						public ServiceRuleManager get() {
							return Holder.newServiceRuleManager();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisContextProperties", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisContextProperties.class, new Supplier<PolarisContextProperties>() {
						@Override
						public PolarisContextProperties get() {
							return Holder.getPolarisContextProperties();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisLoadBalancerAutoConfiguration", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name, BeanDefinitionBuilder.genericBeanDefinition(
					PolarisLoadBalancerAutoConfiguration.class).getBeanDefinition());
		});
	}

	private void registerPolarisSDKContextManager(ApplicationContext context) {
		registerBean(context, "polarisSDKContextManager", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisSDKContextManager.class, new Supplier<PolarisSDKContextManager>() {
						@Override
						public PolarisSDKContextManager get() {
							return Holder.getContextManager();
						}
					}).getBeanDefinition());
		});
	}
}
