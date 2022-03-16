package cn.polarismesh.pinpoint.common;

import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

public abstract class AbstractAroundInterceptor implements AroundInterceptor {

    private final AbstractInterceptor interceptor;

    public AbstractAroundInterceptor() {
        buildInterceptors();
        interceptor = InterceptorFactory.getInterceptor(getClass());
    }

    protected abstract void buildInterceptors();

    @Override
    public void before(Object target, Object[] args) {
        interceptor.before(target, args);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        interceptor.after(target, args, result, throwable);
    }
}
