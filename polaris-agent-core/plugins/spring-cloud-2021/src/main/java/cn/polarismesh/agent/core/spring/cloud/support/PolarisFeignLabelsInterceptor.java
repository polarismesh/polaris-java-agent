package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.BeforePolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContext;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polaris Feign Labels 拦截器类
 *
 * @author zhuyuhan
 */
public class PolarisFeignLabelsInterceptor implements BeforePolarisInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisFeignLabelsInterceptor.class);

    @Override
    public void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties polarisAgentProperties) {
        if (args.length == 2 && args[0] instanceof RequestTemplate) {
            RequestTemplate requestTemplate = (RequestTemplate) args[0];
            InvokeContext invokeContext = InvokeContextHolder.get();
            String path = requestTemplate.path();
            String method = requestTemplate.method();
            invokeContext.setPath(path);
            invokeContext.setMethod(method);
            LOGGER.info("success to get feign invoke path:{}, method:{}", path, method);
        }
    }
}
