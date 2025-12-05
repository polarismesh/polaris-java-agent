/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 * Copyright (C) 2021 Tencent. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class NacosBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final Set<String> nacosBeans = new HashSet<>();

    public NacosBeanDefinitionRegistryPostProcessor() {
        nacosBeans.add("com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.endpoint.NacosDiscoveryEndpointAutoConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.discovery.NacosDiscoveryHeartBeatConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.discovery.reactive.NacosReactiveDiscoveryClientConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.discovery.configclient.NacosConfigServerAutoConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.loadbalancer.LoadBalancerNacosAutoConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.NacosServiceAutoConfiguration");
        nacosBeans.add("com.alibaba.cloud.nacos.util.UtilIPv6AutoConfiguration");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // remove all spring cloud alibaba discovery related beanDefinitions.
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            if (nacosBeans.contains(bd.getFactoryBeanName()) || nacosBeans.contains(bd.getBeanClassName())) {
                registry.removeBeanDefinition(beanName);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //do nothing
    }
}
