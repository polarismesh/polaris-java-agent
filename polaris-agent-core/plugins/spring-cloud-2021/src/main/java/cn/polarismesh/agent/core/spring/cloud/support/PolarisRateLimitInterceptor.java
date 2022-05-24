package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.BeforePolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.ratelimit.PolarisRateLimit;

/**
 * Polaris RateLimit 拦截器
 *
 * @author zhuyuhan
 */
public class PolarisRateLimitInterceptor implements BeforePolarisInterceptor {

    @Override
    public void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties polarisAgentProperties) {
        PolarisRateLimit.judge(InvokeContextHolder.get());
    }
}

