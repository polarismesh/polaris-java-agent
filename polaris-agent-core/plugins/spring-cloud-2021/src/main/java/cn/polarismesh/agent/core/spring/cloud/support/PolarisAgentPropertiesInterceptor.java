package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.BeforePolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisContext;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAPIFactory;
import cn.polarismesh.agent.core.spring.cloud.context.factory.PolarisAgentPropertiesFactory;
import cn.polarismesh.agent.core.spring.cloud.util.HostUtils;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.context.GenericReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
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
            port = applicationContext.getEnvironment().getProperty("server.port");
            service = applicationContext.getEnvironment().getProperty("spring.application.name");
            host = applicationContext.getEnvironment().getProperty("spring.cloud.client.ip-address");
            Assert.notNull(port, "the server port can't be null, please check your server config");
            Assert.notNull(service, "the application name can't be null, please check your spring config");

            log.info("Polaris service is set with port: {}", port);
            log.info("Polaris service is set with service: {}", service);
            log.info("Polaris service is set with host: {}", host);

            // get init info from system
            String host = HostUtils.getHost();
            String namespace = System.getProperty("polaris.namespace");
            String serverAddress = System.getProperty("polaris.server.address");
            Assert.notNull(serverAddress, "the polaris server address can't be null, please check your polaris agent parameter");
            if (StringUtils.isEmpty(namespace)) {
                namespace = "default";
                log.warn("the input namespace is empty, use default instead");
            }

            // init polaris config and reserve
            PolarisAgentProperties polarisAgentProperties = new PolarisAgentProperties();
            polarisAgentProperties.setHost(host);
            polarisAgentProperties.setPort(Integer.valueOf(port));
            polarisAgentProperties.setProtocol("grpc");
            polarisAgentProperties.setNamespace(namespace);
            polarisAgentProperties.setService(service);
            polarisAgentProperties.setServerAddress(serverAddress);
            PolarisAgentPropertiesFactory.setPolarisAgentProperties(polarisAgentProperties);

            // init polarisContext and api
            PolarisContext polarisContext = new PolarisContext(polarisAgentProperties);
            PolarisAPIFactory.init(polarisContext);
        }
    }
}
