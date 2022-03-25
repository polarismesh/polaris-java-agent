package cn.polarismesh.agent.pinpoint.dubbo2.Interceptor;

import cn.polarismesh.pinpoint.common.AbstractAroundInterceptor;

import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractDubboInterceptor extends AbstractAroundInterceptor {

    private static final AtomicBoolean built = new AtomicBoolean(false);

    @Override
    protected void buildInterceptors() {
        if (!built.get()) {
            synchronized (AbstractDubboInterceptor.class) {
                if (!built.get()) {
                    InterceptorBuilder.buildInterceptors();
                    built.set(true);
                }
            }
        }
    }
}
