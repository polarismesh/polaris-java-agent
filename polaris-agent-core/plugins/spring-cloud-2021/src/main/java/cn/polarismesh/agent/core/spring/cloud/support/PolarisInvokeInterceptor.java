package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.AroundPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.observability.PolarisServiceObservability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Polaris Invoke 服务调用拦截类
 *
 * @author zhuyuhan
 */
public class PolarisInvokeInterceptor implements AroundPolarisInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisInvokeInterceptor.class);

    private Long invokeTime;

    @Override
    public void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties polarisAgentProperties) {
        invokeTime = System.currentTimeMillis();
        if (args.length == 3 && args[2] instanceof ClientHttpResponse) {
            ClientHttpResponse response = (ClientHttpResponse) args[2];
            try {
                InvokeContextHolder.get().setStatus(response.getRawStatusCode());
            } catch (IOException e) {
                LOGGER.error("fail to update response status for restTemplate with io exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
        PolarisServiceObservability.updateServiceCallResult(System.currentTimeMillis() - invokeTime, throwable, InvokeContextHolder.get().getStatus(), polarisAgentProperties);
    }

}
