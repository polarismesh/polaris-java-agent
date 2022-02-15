package cn.polarismesh.agent.pinpoint.dubbox.Interceptor;

import cn.polarismesh.agent.plugin.dubbox.interceptor.AbstractInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

public class DubboInvokeInterceptor implements AroundInterceptor {

    private AbstractInterceptor interceptor = InterceptorFactory.getInterceptor(this.getClass());

    @Override
    public void before(Object target, Object[] args) {
        interceptor.before(target, args);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        interceptor.after(target, args, result, throwable);
    }
}
