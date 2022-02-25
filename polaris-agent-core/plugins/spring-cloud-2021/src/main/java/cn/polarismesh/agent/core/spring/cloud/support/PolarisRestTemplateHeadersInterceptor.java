package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.BeforePolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.constant.PolarisServiceConstants;
import cn.polarismesh.agent.core.spring.cloud.context.InvokeContextHolder;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Polaris 服务调用restTemplate header拦截器
 *
 * @author zhuyuhan
 */
public class PolarisRestTemplateHeadersInterceptor implements BeforePolarisInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisRestTemplateHeadersInterceptor.class);

    @Override
    public void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties polarisAgentProperties) {
        // add headers to polaris metadata
        if (target instanceof ClientHttpRequest) {
            ClientHttpRequest request = (ClientHttpRequest) target;
            Map<String, String> metadata = new HashMap<>();
            HttpHeaders headers = request.getHeaders();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getKey().contains(PolarisServiceConstants.HeaderName.METADATA_HEADER)) {
                    metadata.put(header.getKey(), header.getValue().get(0));
                }
            }
            InvokeContextHolder.get().setMetadata(metadata);
            LOGGER.info("success to set Polaris router metadata from header of RestTemplate");
        }
    }
}
