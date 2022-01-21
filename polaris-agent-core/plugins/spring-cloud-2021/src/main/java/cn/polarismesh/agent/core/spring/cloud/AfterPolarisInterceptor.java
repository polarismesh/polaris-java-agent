package cn.polarismesh.agent.core.spring.cloud;

import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;

/**
 * core after Interceptor
 *
 * @author zhuyuhan
 */
public interface AfterPolarisInterceptor extends AroundPolarisInterceptor {

    default void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties polarisAgentProperties) {
    }

}
