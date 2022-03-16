package cn.polarismesh.agent.plugin.dubbox.interceptor;

import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisRegistryFactory;
import cn.polarismesh.agent.plugin.dubbox.utils.ReflectUtil;
import cn.polarismesh.common.interceptor.AbstractInterceptor;

/**
 * interceptor for com.alibaba.dubbo.registry.integration.RegistryProtocol#setRegistryFactory(com.alibaba.dubbo.registry.RegistryFactory)
 */
public class DubboRegistryInterceptor implements AbstractInterceptor {

    //private static final Logger LOGGER = LoggerFactory.getLogger(DubboRegistryInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
    }

    /**
     * 替换registryFactory为PolarisRegistryFactory
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        //LOGGER.info("[POLARIS] set {}.registryFactory as PolarisRegistryFactory", target.getClass());
        ReflectUtil.setValueByFieldName(target, "registryFactory", new PolarisRegistryFactory());
    }
}