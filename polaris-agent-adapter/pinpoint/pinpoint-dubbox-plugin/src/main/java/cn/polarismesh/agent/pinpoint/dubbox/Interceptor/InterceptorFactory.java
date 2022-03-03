package cn.polarismesh.agent.pinpoint.dubbox.Interceptor;

import cn.polarismesh.agent.plugin.dubbox.interceptor.AbstractInterceptor;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    }

    static AbstractInterceptor getInterceptor(Class<?> clazz) {
        return interceptorCache.get(clazz);
    }
}
