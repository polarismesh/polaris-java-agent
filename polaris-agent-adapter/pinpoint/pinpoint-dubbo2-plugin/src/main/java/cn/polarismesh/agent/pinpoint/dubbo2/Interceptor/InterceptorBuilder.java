package cn.polarismesh.agent.pinpoint.dubbo2.Interceptor;


import cn.polarismesh.agent.plugin.dubbo2.interceptor.*;
import cn.polarismesh.pinpoint.common.InterceptorFactory;

public class InterceptorBuilder {

    static void buildInterceptors() {
        DubboConfigCenterInterceptor dubboConfigCenterInterceptor = new DubboConfigCenterInterceptor();
        DubboMetadataCenterInterceptor dubboMetadataCenterInterceptor = new DubboMetadataCenterInterceptor();
        DubboRegistryInterceptor dubboRegistryInterceptor = new DubboRegistryInterceptor();
        DubboDiscoveryInterceptor dubboDiscoveryInterceptor = new DubboDiscoveryInterceptor();
        DubboRateLimitInterceptor dubboRateLimitInterceptor = new DubboRateLimitInterceptor();
        DubboCreateURLInterceptor dubboCreateURLInterceptor = new DubboCreateURLInterceptor();
        DubboReportInvokeInterceptor dubboReportInvokeInterceptor = new DubboReportInvokeInterceptor();
        DubboRouterInterceptor dubboRouterInterceptor = new DubboRouterInterceptor();
        DubboLoadBalanceInterceptor dubboLoadBalanceInterceptor = new DubboLoadBalanceInterceptor();

        InterceptorFactory.addInterceptor(DubboConfigInterceptor.class, dubboConfigCenterInterceptor);
        InterceptorFactory.addInterceptor(DubboMetadataInterceptor.class, dubboMetadataCenterInterceptor);
        InterceptorFactory.addInterceptor(DubboRegistryFactoryInterceptor.class, dubboRegistryInterceptor);
        InterceptorFactory.addInterceptor(DubboRegistryDirectoryInterceptor.class, dubboDiscoveryInterceptor);
        InterceptorFactory.addInterceptor(DubboExporterInterceptor.class, dubboRateLimitInterceptor);
        InterceptorFactory.addInterceptor(DubboUrlInterceptor.class, dubboCreateURLInterceptor);
        InterceptorFactory.addInterceptor(DubboInvokeInterceptor.class, dubboReportInvokeInterceptor);
        InterceptorFactory.addInterceptor(DubboAbstractDirectoryInterceptor.class, dubboRouterInterceptor);
        InterceptorFactory.addInterceptor(DubboExtensionLoaderInterceptor.class, dubboLoadBalanceInterceptor);
    }
}
