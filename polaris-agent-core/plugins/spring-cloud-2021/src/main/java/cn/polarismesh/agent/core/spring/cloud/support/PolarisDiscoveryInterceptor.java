package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.AfterPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.discovery.PolarisDiscoveryClient;
import cn.polarismesh.agent.core.spring.cloud.discovery.PolarisServiceDiscovery;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;

/**
 * Polaris 服务发现拦截器
 */
public class PolarisDiscoveryInterceptor implements AfterPolarisInterceptor {

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
        if (target instanceof CompositeDiscoveryClient) {
            // log
            LogUtils.logTargetFound(target);
            // add polaris discoveryClient to first index
            CompositeDiscoveryClient compositeDiscoveryClient = (CompositeDiscoveryClient) target;
            compositeDiscoveryClient.getDiscoveryClients().add(0, new PolarisDiscoveryClient(new PolarisServiceDiscovery()));
        }
    }

}
