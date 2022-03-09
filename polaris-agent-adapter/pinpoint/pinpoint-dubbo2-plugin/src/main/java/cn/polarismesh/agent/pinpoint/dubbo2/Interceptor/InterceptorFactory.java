package cn.polarismesh.agent.pinpoint.dubbo2.Interceptor;

import cn.polarismesh.agent.plugin.dubbo2.interceptor.AbstractInterceptor;

import java.util.HashMap;
import java.util.Map;

class InterceptorFactory {
    private static final Map<Class<?>, AbstractInterceptor> interceptorCache = new HashMap<>();

    static {
        interceptorCache.put(DubboInvokerInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboInvokerInterceptor());
        interceptorCache.put(DubboClusterInvokerInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboClusterInvokerInterceptor());
        interceptorCache.put(DubboInvokeInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboInvokeInterceptor());
        interceptorCache.put(DubboRegistryInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboRegistryInterceptor());
        interceptorCache.put(DubboConfigInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboConfigInterceptor());
        interceptorCache.put(DubboMetadataInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboMetadataInterceptor());
        interceptorCache.put(DubboExporterInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboExporterInterceptor());
        interceptorCache.put(DubboUrlInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboUrlInterceptor());
    }

    static AbstractInterceptor getInterceptor(Class<?> clazz) {
        return interceptorCache.get(clazz);
    }
}
