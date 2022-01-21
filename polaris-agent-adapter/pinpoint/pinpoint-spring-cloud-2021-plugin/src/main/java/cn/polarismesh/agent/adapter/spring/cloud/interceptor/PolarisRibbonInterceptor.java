package cn.polarismesh.agent.adapter.spring.cloud.interceptor;

import cn.polarismesh.agent.core.spring.cloud.AfterPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAgentPropertiesFactory;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

/**
 * Polaris Ribbon负载均衡拦截器
 *
 * @author zhuyuhan
 */
public class PolarisRibbonInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // log
        LogUtils.logTargetFound(target);
        // do registry
        AfterPolarisInterceptor polarisInterceptor = new cn.polarismesh.agent.core.spring.cloud.support.PolarisRibbonInterceptor();
        polarisInterceptor.afterInterceptor(target, args, result, throwable, PolarisAgentPropertiesFactory.getPolarisAgentProperties());
    }
}

