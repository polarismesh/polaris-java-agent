package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.plugin.dubbo2.utils.ReflectUtil;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.apache.dubbo.config.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * interceptor for org.apache.dubbo.config.context.ConfigManager#getMetadataConfigs()
 */
public class DubboMetadataCenterInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboMetadataCenterInterceptor.class);

    private static final String METADATA_REPORT_KEY = "metadata-report";

    /**
     * 在getMetadataConfigs()处拦截，清除掉configsCache中关于metadata-report的信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public void before(Object target, Object[] args) {
        Map<String, Map<String, AbstractConfig>> configsCache =
                (Map<String, Map<String, AbstractConfig>>) ReflectUtil.getObjectByFieldName(target, "configsCache");
        if (null == configsCache) {
            LOGGER.error("metadata-report: get configsCache fail, object is null");
            return;
        }
        configsCache.remove(METADATA_REPORT_KEY);
        LOGGER.info("clean metadata-report key in dubbo ConfigManager");
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}