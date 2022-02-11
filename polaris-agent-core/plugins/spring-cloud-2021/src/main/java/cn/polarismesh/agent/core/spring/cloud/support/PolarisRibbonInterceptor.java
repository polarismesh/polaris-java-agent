package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.AfterPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAPIFactory;
import cn.polarismesh.agent.core.spring.cloud.discovery.PolarisDiscoveryHandler;
import cn.polarismesh.agent.core.spring.cloud.loadbalance.ribbon.PolarisRoutingLoadBalancer;
import cn.polarismesh.agent.core.spring.cloud.loadbalance.ribbon.PolarisServerList;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.DummyPing;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.LoadBalancerContext;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

/**
 * Polaris Ribbon负载均衡拦截器
 */
public class PolarisRibbonInterceptor implements AfterPolarisInterceptor {

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
        // init ribbon context
        IClientConfig clientConfig = (IClientConfig) args[1];
        ZoneAvoidanceRule rule = new ZoneAvoidanceRule();
        IPing ping = new DummyPing();
        rule.initWithNiwsConfig(clientConfig);

        // init ribbon serverList
        PolarisServerList serverList = new PolarisServerList(new PolarisDiscoveryHandler());
        serverList.initWithNiwsConfig(clientConfig);

        // init polaris loadBalancer
        PolarisRoutingLoadBalancer routingLoadBalancer =
                new PolarisRoutingLoadBalancer(
                        clientConfig,
                        rule,
                        ping,
                        serverList,
                        PolarisAPIFactory.getRouterApi(),
                        polarisAgentProperties
                );

        // replace loadBalancer
        LoadBalancerContext loadBalancerContext = (LoadBalancerContext) target;
        loadBalancerContext.setLoadBalancer(routingLoadBalancer);
    }

}
