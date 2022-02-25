package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.AfterPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisContext;
import cn.polarismesh.agent.core.spring.cloud.registry.PolarisRegistration;
import cn.polarismesh.agent.core.spring.cloud.registry.PolarisServiceRegistry;
import cn.polarismesh.agent.core.spring.cloud.registry.Registry;
import cn.polarismesh.agent.core.spring.cloud.util.LogUtils;
import org.springframework.boot.web.reactive.context.GenericReactiveWebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * Polaris Ribbon Server 实现类
 */
public class PolarisRegistryInterceptor implements AfterPolarisInterceptor, Registry {

    private Registry registry = null;

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
        try {
            if (target instanceof GenericWebApplicationContext || target instanceof GenericReactiveWebApplicationContext) {
                LogUtils.logTargetFound(target);
                PolarisContext polarisContext = new PolarisContext(polarisAgentProperties);
                initFromContext(polarisContext);
                interceptorInner();
            }
        } catch (Throwable e) {
            LogUtils.logInterceptError("PolarisRegistryInterceptor", e.getMessage());
        }
    }

    @Override
    public void register() {
        registry.register();
    }

    @Override
    public void deregister() {
        Runtime.getRuntime().addShutdownHook(new Thread(registry::deregister));
    }

    private void initFromContext(PolarisContext polarisContext) {
        PolarisRegistration registration = new PolarisRegistration(polarisContext);
        registry = new PolarisServiceRegistry(registration);
    }

    private void interceptorInner() {
        register();
        deregister();
    }

}
