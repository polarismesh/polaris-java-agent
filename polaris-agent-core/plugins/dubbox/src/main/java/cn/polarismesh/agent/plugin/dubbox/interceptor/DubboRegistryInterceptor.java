package cn.polarismesh.agent.plugin.dubbox.interceptor;

import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisRegistryFactory;
import cn.polarismesh.agent.plugin.dubbox.utils.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务注册拦截器
 */
public class DubboRegistryInterceptor implements AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboRegistryInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
    }

    /**
     * 替换registryFactory为PolarisRegistryFactory
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        LOGGER.info("set {}.registryFactory as PolarisRegistryFactory", target.getClass());
        ReflectUtil.setValueByFieldName(target, "registryFactory", new PolarisRegistryFactory());
    }
}