package cn.polarismesh.agent.core.spring.cloud.router;

import org.springframework.cloud.loadbalancer.cache.DefaultLoadBalancerCache;

/**
 * Polaris 服务路由缓存
 *
 * @author zhuyuhan
 */
public class PolarisLoadBalancerCache extends DefaultLoadBalancerCache {

    public PolarisLoadBalancerCache(String name) {
        super(name);
    }

    public PolarisLoadBalancerCache(String name, long evictMs, boolean allowNullValues) {
        super(name, evictMs, allowNullValues);
    }

    public PolarisLoadBalancerCache(String name, boolean allowNullValues) {
        super(name, allowNullValues);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return (T) (PolarisServiceRouter.getInstances((String) key));
    }
}
