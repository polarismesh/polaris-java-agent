package cn.polarismesh.agent.pinpoint.dubbo2.Interceptor;

import cn.polarismesh.agent.plugin.dubbo2.interceptor.AbstractInterceptor;

import java.util.HashMap;
import java.util.Map;

class InterceptorFactory {
    private static final Map<Class<?>, AbstractInterceptor> interceptorCache = new HashMap<>();

    static {
        interceptorCache.put(DubboInvokerInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboInvokerInterceptor());
        interceptorCache.put(DubboProviderInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboProviderInterceptor());
        interceptorCache.put(DubboClusterInvokerInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboClusterInvokerInterceptor());
        interceptorCache.put(DubboInvokeInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboInvokeInterceptor());
        interceptorCache.put(DubboLoadBalanceInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboLoadBalanceInterceptor());
    }

    static AbstractInterceptor getInterceptor(Class<?> clazz) {
        return interceptorCache.get(clazz);
    }
}
