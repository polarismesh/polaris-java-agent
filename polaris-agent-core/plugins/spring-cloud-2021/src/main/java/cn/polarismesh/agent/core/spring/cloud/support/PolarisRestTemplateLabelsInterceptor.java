package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.BeforePolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContext;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.net.URI;

/**
 * Polaris RestTemplate labels 拦截器
 *
 * @author zhuyuhan
 */
public class PolarisRestTemplateLabelsInterceptor implements BeforePolarisInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisRestTemplateLabelsInterceptor.class);

    @Override
    public void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties polarisAgentProperties) {
        if (args.length == 4 && (args[0] instanceof URI) && args[1] instanceof HttpMethod) {
            URI uri = (URI) args[0];
            HttpMethod httpMethod = (HttpMethod) args[1];
            InvokeContext invokeContext = InvokeContextHolder.get();
            String path = uri.getPath();
            String method = httpMethod.name();
            invokeContext.setPath(path);
            invokeContext.setMethod(method);
            LOGGER.info("success to get restTemplate invoke path:{}, method:{}", path, method);
        }
    }
}

