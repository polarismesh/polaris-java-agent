package cn.polarismesh.agent.core.spring.cloud;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;

/**
 * core around Interceptor
 *
 * @author zhuyuhan
 */
public interface AroundPolarisInterceptor extends PolarisInterceptor {

    void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties polarisAgentProperties);

    void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties);

}
