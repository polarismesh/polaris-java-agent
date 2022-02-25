package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.AroundPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.constant.PolarisServiceConstants;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContext;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Polaris Feign 服务调用响应状态获取拦截类
 *
 * @author zhuyuhan
 */
public class PolarisFeignExecuteInterceptor implements AroundPolarisInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisFeignExecuteInterceptor.class);

    @Override
    public void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties polarisAgentProperties) {
        // add headers to polaris metadata
        if (args[0] instanceof Request) {
            Request request = (Request) args[0];
            Map<String, String> metadata = new HashMap<>();
            Map<String, Collection<String>> headers = request.headers();
            for (Map.Entry<String, Collection<String>> header : headers.entrySet()) {
                if (header.getKey().contains(PolarisServiceConstants.HeaderName.METADATA_HEADER)) {
                    metadata.put(header.getKey(), new ArrayList<>(header.getValue()).get(0));
                }
            }
            InvokeContextHolder.get().setMetadata(metadata);
            LOGGER.info("success to set Polaris router metadata from header of Feign");
        }
    }

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
        // get status from feign response
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
