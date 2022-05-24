package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.AfterPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContext;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.ratelimit.PolarisRateLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

/**
 * Polaris Feign Invoke 获取服务信息的拦截类
 *
 * @author zhuyuhan
 */
public class PolarisServiceInfoInterceptor implements AfterPolarisInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisServiceInfoInterceptor.class);

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
        ServiceInstance instance = (ServiceInstance) result;
        if (instance == null) {
            LOGGER.info("fail to reserve ServiceInstance for current thread");
            return;
        }
        InvokeContext invokeContext = InvokeContextHolder.get();
        invokeContext.setServiceInstance(instance);
        LOGGER.info("success to reserve ServiceInstance: {} for current thread", instance.getServiceId() + ":" + instance.getHost() + ":" + instance.getPort());
        // judge rate limit info
        PolarisRateLimit.judge(invokeContext);
    }

}
