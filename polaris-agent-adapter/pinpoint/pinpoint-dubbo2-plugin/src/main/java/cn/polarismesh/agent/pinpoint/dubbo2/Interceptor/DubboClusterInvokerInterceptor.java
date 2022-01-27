package cn.polarismesh.agent.pinpoint.dubbo2.Interceptor;

import cn.polarismesh.agent.plugin.dubbo2.interceptor.AbstractInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

public class DubboClusterInvokerInterceptor implements AroundInterceptor {

    private AbstractInterceptor interceptor = InterceptorFactory.getInterceptor(DubboClusterInvokerInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
        interceptor.before(target, args);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        interceptor.after(target, args, result, throwable);
    }
}
