package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.common.config.AgentConfig;
import cn.polarismesh.agent.core.spring.cloud.BeforePolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAgentPropertiesFactory;
import cn.polarismesh.agent.core.spring.cloud.util.HostUtils;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.context.GenericReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.context.support.GenericWebApplicationContext;

import static cn.polarismesh.agent.core.spring.cloud.constant.PolarisServiceConstants.*;

/**
 * Polaris 服务属性初始化拦截器
 */
public class PolarisAgentPropertiesInterceptor implements BeforePolarisInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PolarisAgentPropertiesInterceptor.class);

    @Override
    public void beforeInterceptor(Object target, Object[] args, PolarisAgentProperties agentProperties) {
        // check if servlet applicationContext or reactive applicationContext
        Object configurableContext = args[0];
        if (configurableContext instanceof GenericWebApplicationContext || configurableContext instanceof GenericReactiveWebApplicationContext) {

            // log
            LogUtils.logTargetFound(target);

            // convert to applicationContext, actual AnnotationConfigServletWebApplicationContext or AnnotationConfigReactiveWebServerApplicationContext
            ApplicationContext applicationContext = (ApplicationContext) configurableContext;

            // get basic info from applicationContext
            PORT = applicationContext.getEnvironment().getProperty("server.port");
            SERVICE = applicationContext.getEnvironment().getProperty("spring.application.name");
            HOST = applicationContext.getEnvironment().getProperty("spring.cloud.client.ip-address");
            if (PORT == null) {
                log.warn("the server port is empty loaded from application config, use '8080' instead");
            }
            Assert.notNull(SERVICE, "the application name can't be null, please check your spring config");

            log.info("Polaris service is set with name: {}, host: {}, port: {}", SERVICE, HOST, PORT);

            // get init info from system
            String host = HostUtils.getHost();
            String namespace = System.getProperty(AgentConfig.KEY_NAMESPACE);
            String serverAddress = System.getProperty(AgentConfig.KEY_REGISTRY);
//            String version = System.getProperty(AgentConfig.KEY_VERSION);
            String ttl = System.getProperty(AgentConfig.KEY_HEALTH_TTL);
            String token = System.getProperty(AgentConfig.KEY_TOKEN);
            Assert.notNull(serverAddress, "the polaris server address can't be null, please check your polaris agent parameter");
            if (namespace == null || "".equals(namespace)) {
                log.warn("the input namespace is empty, use 'default' instead");
            }

            // init polaris config and reserve
            PolarisAgentProperties polarisAgentProperties =
                    PolarisAgentProperties.builder()
                            .withHost(host)
                            .withPort(PORT)
                            .withServerAddress(serverAddress)
                            .withNamespace(namespace)
                            .withService(SERVICE)
//                            .withVersion(version)
                            .withServerToken(token)
                            .withTtl(ttl)
                            .build();
            PolarisAgentPropertiesFactory.setPolarisAgentProperties(polarisAgentProperties);
        }
    }
}
