package cn.polarismesh.agent.core.spring.cloud;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;

/**
 * core before Interceptor
 *
 * @author zhuyuhan
 */
public interface BeforePolarisInterceptor extends AroundPolarisInterceptor {

    default void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
    }

}
