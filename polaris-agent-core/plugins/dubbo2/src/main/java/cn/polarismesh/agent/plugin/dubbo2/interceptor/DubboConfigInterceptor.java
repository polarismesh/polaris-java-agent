package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.plugin.dubbo2.utils.ReflectUtil;
import org.apache.dubbo.config.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ConfigManager拦截器
 */
public class DubboConfigInterceptor implements AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboConfigInterceptor.class);

    private static final String CONFIG_CENTER_KEY = "config-center";


    /**
     * 在getConfigCenters()处拦截，清除掉configsCache中关于config-center的信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public void before(Object target, Object[] args) {
        Map<String, Map<String, AbstractConfig>> configsCache =
                (Map<String, Map<String, AbstractConfig>>) ReflectUtil.getObjectByFieldName(target, "configsCache");
        if (null == configsCache) {
            LOGGER.error("config-center: get configsCache fail, object is null");
            return;
        }
        configsCache.remove(CONFIG_CENTER_KEY);
        LOGGER.info("clean config-center key in dubbo ConfigManager");
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}