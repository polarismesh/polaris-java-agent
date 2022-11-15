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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.common.PolarisOperator;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementReporterProperties;
import com.tencent.cloud.rpc.enhancement.feign.DefaultEnhancedFeignPluginRunner;
import com.tencent.cloud.rpc.enhancement.feign.EnhancedFeignBeanPostProcessor;
import com.tencent.cloud.rpc.enhancement.feign.EnhancedFeignPluginRunner;
import com.tencent.cloud.rpc.enhancement.feign.plugin.EnhancedFeignPlugin;
import com.tencent.cloud.rpc.enhancement.feign.plugin.reporter.ExceptionPolarisReporter;
import com.tencent.cloud.rpc.enhancement.feign.plugin.reporter.SuccessPolarisReporter;
import com.tencent.cloud.rpc.enhancement.resttemplate.BlockingLoadBalancerClientAspect;
import com.tencent.cloud.rpc.enhancement.resttemplate.EnhancedRestTemplateReporter;
import com.tencent.polaris.api.core.ConsumerAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class RpcEnhancementHandler implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcEnhancementHandler.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;

		registerRestTemplateReporter(context);
		registerFeignReporter(context);
	}

	private void registerRestTemplateReporter(ConfigurableApplicationContext context) {
		EnhancedRestTemplateReporter reporter =
				new EnhancedRestTemplateReporter(Holder.getRpcEnhancementReporterProperties(),
						PolarisOperator.getInstance().getConsumerAPI());

		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();

		beanFactory.registerBeanDefinition("sctErrorHandlerForRestTemplate", buildRestTemplateReportBeanDefinition(context, reporter));
		beanFactory.registerBeanDefinition("sctBlockingLoadBalancerClientAspect",
				buildBlockingLoadBalancerClientAspectBean());

		LOGGER.info("[PolarisAgent] success inject Spring Cloud Tencent RestTemplate reporter");
	}

	private BeanDefinition buildRestTemplateReportBeanDefinition(ConfigurableApplicationContext context,
			EnhancedRestTemplateReporter reporter) {
		return BeanDefinitionBuilder.genericBeanDefinition(SmartInitializingSingleton.class, () -> () -> {
			Map<String, RestTemplate> beans = context.getBeanFactory().getBeansOfType(RestTemplate.class);
			for (RestTemplate restTemplate : beans.values()) {
				restTemplate.setErrorHandler(reporter);
			}
		}).getBeanDefinition();
	}

	private BeanDefinition buildBlockingLoadBalancerClientAspectBean() {
		return BeanDefinitionBuilder.genericBeanDefinition(BlockingLoadBalancerClientAspect.class).getBeanDefinition();
	}

	private void registerFeignReporter(ConfigurableApplicationContext context) {
		EnhancedFeignPluginRunner pluginRunner =
				enhancedFeignPluginRunner(Holder.getRpcEnhancementReporterProperties(), PolarisOperator.getInstance()
						.getConsumerAPI());
		EnhancedFeignBeanPostProcessor processor = new EnhancedFeignBeanPostProcessor(pluginRunner);
		processor.setBeanFactory(context.getBeanFactory());
		context.getBeanFactory().addBeanPostProcessor(processor);

		LOGGER.info("[PolarisAgent] success inject Spring Cloud Tencent FeignClient reporter");
	}

	private DefaultEnhancedFeignPluginRunner enhancedFeignPluginRunner(RpcEnhancementReporterProperties properties, ConsumerAPI consumerAPI) {
		List<EnhancedFeignPlugin> ret = new ArrayList<>();
		ret.add(new SuccessPolarisReporter(properties, consumerAPI));
		ret.add(new ExceptionPolarisReporter(properties, consumerAPI));
		return new DefaultEnhancedFeignPluginRunner(ret);
	}

}
