package cn.polarismesh.agent.plugin.spring.cloud.common;

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
