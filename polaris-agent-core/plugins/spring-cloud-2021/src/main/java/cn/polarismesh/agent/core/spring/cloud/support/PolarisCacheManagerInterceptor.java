package cn.polarismesh.agent.core.spring.cloud.support;

import cn.polarismesh.agent.core.spring.cloud.AfterPolarisInterceptor;
import cn.polarismesh.agent.core.spring.cloud.context.PolarisAgentProperties;
import cn.polarismesh.agent.core.spring.cloud.router.PolarisLoadBalancerCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cloud.loadbalancer.cache.DefaultLoadBalancerCacheManager;
import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Polaris 服务发现缓存拦截器
 *
 * @author zhuyuhan
 */
public class PolarisCacheManagerInterceptor implements AfterPolarisInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisCacheManagerInterceptor.class);

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable, PolarisAgentProperties polarisAgentProperties) {
        if (target instanceof DefaultLoadBalancerCacheManager) {
            DefaultLoadBalancerCacheManager manager = (DefaultLoadBalancerCacheManager) target;
            if (manager.getCache(CachingServiceInstanceListSupplier.SERVICE_INSTANCE_CACHE_NAME) != null) {
                try {
                    Field cacheMap = DefaultLoadBalancerCacheManager.class.getDeclaredField("cacheMap");
                    cacheMap.setAccessible(true);
                    Map<String, Cache> stringCacheMap = (Map<String, Cache>) cacheMap.get(manager);
                    stringCacheMap.put(CachingServiceInstanceListSupplier.SERVICE_INSTANCE_CACHE_NAME,
                            new PolarisLoadBalancerCache(
                                    CachingServiceInstanceListSupplier.SERVICE_INSTANCE_CACHE_NAME));
                    LOGGER.info("replace default DefaultLoadBalancerCache with PolarisLoadBalancerCache successfully");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
