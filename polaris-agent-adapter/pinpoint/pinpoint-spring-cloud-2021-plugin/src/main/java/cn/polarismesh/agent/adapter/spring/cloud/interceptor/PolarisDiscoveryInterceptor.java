package cn.polarismesh.agent.adapter.spring.cloud.interceptor;

import cn.polarismesh.agent.core.spring.cloud.AfterPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAgentPropertiesFactory;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

/**
 * Polaris服务发现拦截器
 *
 * @author zhuyuhan
 */
public class PolarisDiscoveryInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // do interceptor
        AfterPolarisInterceptor afterPolarisInterceptor = new cn.polarismesh.agent.core.spring.cloud.support.PolarisDiscoveryInterceptor();
        afterPolarisInterceptor.afterInterceptor(target, args, result, throwable, PolarisAgentPropertiesFactory.getPolarisAgentProperties());
    }
}

