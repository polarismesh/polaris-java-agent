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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.aware.report;

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import com.tencent.cloud.rpc.enhancement.feign.EnhancedFeignBeanPostProcessor;
import com.tencent.cloud.rpc.enhancement.plugin.DefaultEnhancedPluginRunner;
import com.tencent.cloud.rpc.enhancement.plugin.EnhancedPluginRunner;
import com.tencent.cloud.rpc.enhancement.resttemplate.EnhancedRestTemplateInterceptor;
import com.tencent.polaris.client.api.SDKContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class RpcEnhancementHandler implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcEnhancementHandler.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
		EnhancedPluginRunner runner = newEnhancedPluginRunner(Holder.getContextManager().getSDKContext(), null);

		registerRestTemplateReporter(context, runner);
		registerFeignReporter(context, runner);
	}

	private void registerRestTemplateReporter(ConfigurableApplicationContext context, EnhancedPluginRunner runner) {
		EnhancedRestTemplateInterceptor reporter = new EnhancedRestTemplateInterceptor(runner);
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();

		beanFactory.registerBeanDefinition("springCloudTencentRestTemplateReporter",
				buildRestTemplateReportBeanDefinition(context,
						reporter));

		LOGGER.info("[PolarisAgent] success inject Spring Cloud Tencent RestTemplate reporter");
	}

	private BeanDefinition buildRestTemplateReportBeanDefinition(ConfigurableApplicationContext context,
																 EnhancedRestTemplateInterceptor reporter) {
		return BeanDefinitionBuilder.genericBeanDefinition(SmartInitializingSingleton.class, () -> () -> {
			Map<String, RestTemplate> beans = context.getBeanFactory().getBeansOfType(RestTemplate.class);
			for (RestTemplate restTemplate : beans.values()) {
				restTemplate.getInterceptors().add(reporter);
			}
		}).getBeanDefinition();
	}

	private void registerFeignReporter(ConfigurableApplicationContext context, EnhancedPluginRunner runner) {
		if (!context.getBeanFactory().containsBeanDefinition("feignContext")) {
			return;
		}
		EnhancedFeignBeanPostProcessor processor = new EnhancedFeignBeanPostProcessor(runner);
		processor.setBeanFactory(context.getBeanFactory());
		context.getBeanFactory().addBeanPostProcessor(processor);

		LOGGER.info("[PolarisAgent] success inject Spring Cloud Tencent FeignClient reporter");
	}

	private EnhancedPluginRunner newEnhancedPluginRunner(SDKContext context, Registration registration) {
		return new DefaultEnhancedPluginRunner(Collections.emptyList(), registration, context);
	}

}
