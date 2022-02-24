package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.AfterPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContext;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polaris Feign 服务调用响应状态获取拦截类
 *
 * @author zhuyuhan
 */
public class PolarisFeignInvokeStatusInterceptor implements AfterPolarisInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisFeignInvokeStatusInterceptor.class);

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
        InvokeContext invokeContext = InvokeContextHolder.get();
        if (invokeContext.getServiceInstance() == null) {
            LOGGER.warn("fail to update response status for feign with instance empty");
            return;
        }
        if (result != null) {
            Response response = (Response) result;
            invokeContext.setStatus(response.status());
        }
    }

}
