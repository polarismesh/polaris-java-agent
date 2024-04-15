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

package cn.polarismesh.agent.plugin.spring.cloud.rpc;

import cn.polarismesh.agent.plugin.spring.cloud.base.AbstractContextHandler;
import com.tencent.cloud.plugin.discovery.adapter.config.NacosDiscoveryAdapterAutoConfiguration;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class RpcEnhancementHandler extends AbstractContextHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcEnhancementHandler.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		registerRpcEnhancementAutoConfiguration(applicationContext);
	}

	private void registerRpcEnhancementAutoConfiguration(ApplicationContext context) {
		registerBean(context, "rpcEnhancementAutoConfiguration", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(RpcEnhancementAutoConfiguration.class).getBeanDefinition());
		});
		registerBean(context, "nacosDiscoveryAdapterAutoConfiguration", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(NacosDiscoveryAdapterAutoConfiguration.class).getBeanDefinition());
		});
	}

}
