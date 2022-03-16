package cn.polarismesh.agent.pinpoint.dubbox.Interceptor;

import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboCreateURLInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboDiscoveryInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboLoadBalanceInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboRateLimitInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboRegistryInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboReportInvokeInterceptor;
import cn.polarismesh.agent.plugin.dubbox.interceptor.DubboRouterInterceptor;
import cn.polarismesh.pinpoint.common.InterceptorFactory;

public class InterceptorBuilder {

    static void buildInterceptors() {
        DubboCreateURLInterceptor dubboCreateURLInterceptor = new DubboCreateURLInterceptor();
        DubboDiscoveryInterceptor dubboDiscoveryInterceptor = new DubboDiscoveryInterceptor();
        DubboLoadBalanceInterceptor dubboLoadBalanceInterceptor = new DubboLoadBalanceInterceptor();
        DubboRateLimitInterceptor dubboRateLimitInterceptor = new DubboRateLimitInterceptor();
        DubboReportInvokeInterceptor dubboReportInvokeInterceptor = new DubboReportInvokeInterceptor();
        DubboRouterInterceptor dubboRouterInterceptor = new DubboRouterInterceptor();
        DubboRegistryInterceptor dubboRegistryInterceptor = new DubboRegistryInterceptor();

        InterceptorFactory.addInterceptor(DubboRegistryDirectoryInterceptor.class, dubboDiscoveryInterceptor);
        InterceptorFactory.addInterceptor(DubboUrlInterceptor.class, dubboCreateURLInterceptor);
        InterceptorFactory.addInterceptor(DubboExporterInterceptor.class, dubboRateLimitInterceptor);
        InterceptorFactory.addInterceptor(DubboInvokeInterceptor.class, dubboReportInvokeInterceptor);
        InterceptorFactory.addInterceptor(DubboExtensionLoaderInterceptor.class, dubboLoadBalanceInterceptor);
        InterceptorFactory.addInterceptor(DubboAbstractDirectoryInterceptor.class, dubboRouterInterceptor);
        InterceptorFactory.addInterceptor(DubboRegistryFactoryInterceptor.class, dubboRegistryInterceptor);
    }
}
