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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class NacosBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final Set<String> nacosBeans = new HashSet<>();

    public NacosBeanDefinitionRegistryPostProcessor() {
        nacosBeans.add("nacosAutoServiceRegistration");
        nacosBeans.add("nacosDiscoveryClient");
//        nacosBeans.add("loadBalancerNacosAutoConfiguration");
//        nacosBeans.add("nacosLoadBalancer");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String beanName : nacosBeans) {
            if (registry.containsBeanDefinition(beanName)) {
                registry.removeBeanDefinition(beanName);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //do nothing
    }
}
