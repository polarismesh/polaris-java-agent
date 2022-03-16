package cn.polarismesh.pinpoint.common;

import cn.polarismesh.common.interceptor.AbstractInterceptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InterceptorFactory {

    private static final Map<Class<?>, AbstractInterceptor> interceptorCache = new HashMap<>();

    public static AbstractInterceptor getInterceptor(Class<?> clazz) {
        return interceptorCache.get(clazz);
    }

    public static void addInterceptor(Class<?> clazz, AbstractInterceptor interceptor) {
        interceptorCache.put(clazz, interceptor);
    }

    public static Map<Class<?>, AbstractInterceptor> getInterceptors() {
        return Collections.unmodifiableMap(interceptorCache);
    }


}
