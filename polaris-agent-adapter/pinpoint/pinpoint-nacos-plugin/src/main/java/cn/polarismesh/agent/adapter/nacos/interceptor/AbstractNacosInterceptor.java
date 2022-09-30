package cn.polarismesh.agent.adapter.nacos.interceptor;

import cn.polarismesh.pinpoint.common.AbstractAroundInterceptor;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractNacosInterceptor extends AbstractAroundInterceptor {

    private static final AtomicBoolean built = new AtomicBoolean(false);

    @Override
    protected void buildInterceptors() {
        if (!built.get()) {
            synchronized (AbstractNacosInterceptor.class) {
                if (!built.get()) {
                    InterceptorBuilder.buildInterceptors();
                    built.set(true);
                }
            }
        }
    }
}
