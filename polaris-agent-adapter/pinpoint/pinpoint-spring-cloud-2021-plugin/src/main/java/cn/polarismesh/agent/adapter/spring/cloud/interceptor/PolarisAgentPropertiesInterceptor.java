package cn.polarismesh.agent.adapter.spring.cloud.interceptor;

import cn.polarismesh.agent.core.spring.cloud.AroundPolarisInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

/**
 * Polaris服务属性获取拦截器
 *
 * @author zhuyuhan
 */
public class PolarisAgentPropertiesInterceptor implements AroundInterceptor {

    private final AroundPolarisInterceptor polarisInterceptor = InterceptorFactory.getInterceptor(this.getClass());

    @Override
    public void before(Object target, Object[] args) {
        // do init
        polarisInterceptor.beforeInterceptor(target, args, null);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }

}

