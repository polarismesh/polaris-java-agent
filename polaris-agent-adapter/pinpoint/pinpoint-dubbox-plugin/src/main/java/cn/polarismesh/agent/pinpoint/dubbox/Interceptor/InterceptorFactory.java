package cn.polarismesh.agent.pinpoint.dubbox.Interceptor;

import cn.polarismesh.agent.plugin.dubbox.interceptor.AbstractInterceptor;
import java.util.HashMap;
import java.util.Map;

class InterceptorFactory {

    private static final Map<Class<?>, AbstractInterceptor> interceptorCache = new HashMap<>();

    static {
        interceptorCache.put(DubboInvokerInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbox.interceptor.DubboInvokerInterceptor());
        interceptorCache.put(DubboClusterInvokerInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbox.interceptor.DubboClusterInvokerInterceptor());
        interceptorCache.put(DubboInvokeInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbox.interceptor.DubboInvokeInterceptor());
        interceptorCache.put(DubboRegistryInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbox.interceptor.DubboRegistryInterceptor());
        interceptorCache.put(DubboExporterInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbox.interceptor.DubboExporterInterceptor());
        interceptorCache.put(DubboUrlInterceptor.class,
                new cn.polarismesh.agent.plugin.dubbox.interceptor.DubboUrlInterceptor());
    }

    static AbstractInterceptor getInterceptor(Class<?> clazz) {
        return interceptorCache.get(clazz);
    }
}
