package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.plugin.dubbo2.polaris.loadbalance.LoadBalanceFactory;
import cn.polarismesh.agent.plugin.dubbo2.polaris.loadbalance.PolarisAbstractLoadBalance;
import cn.polarismesh.agent.plugin.dubbo2.utils.ReflectUtil;
import org.apache.dubbo.common.utils.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

import static cn.polarismesh.agent.plugin.dubbo2.constants.DubboConstants.DUBBO_LOADBALANCES;
import static cn.polarismesh.agent.plugin.dubbo2.constants.PolarisConstants.DEFAULT_LOADBALANCE;


/**
 * 服务发现拦截器2：用于将dubbo的LoadBalance替换为自己的LoadBalance
 */
public class DubboLoadBalanceInterceptor implements AroundInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboLoadBalanceInterceptor.class);

    /**
     * 在ExtensionLoader的getOrCreateHolder()方法前进行拦截
     * 若本次调用的目的是获取LoadBalance对象，则替换为Polaris自己定义的LoadBalance
     * 拦截方法：private Holder<Object> getOrCreateHolder(String name)
     *
     * @param target    拦截方法所属的对象
     * @param args      拦截方法的入参  args[0] : 本次调用的name信息，如name="random"时表示要获取RandomLoadBalance对象
     */
    @SuppressWarnings("unchecked")
    @Override
    public void before(Object target, Object[] args) {
        String name = (String) args[0];
        if (!DUBBO_LOADBALANCES.contains(name)) {
            return;
        }

        ConcurrentMap<String, Holder<Object>> cachedInstances =
                (ConcurrentMap<String, Holder<Object>>) ReflectUtil.getObjectByFieldName(target, "cachedInstances");
        if (cachedInstances == null) {
            LOGGER.error("get cachedInstances fail");
            return;
        }

        name = System.getProperty("loadbalance", DEFAULT_LOADBALANCE);
        Holder<Object> holder = cachedInstances.get(name);
        if (holder != null && holder.get() instanceof PolarisAbstractLoadBalance) {
            return;
        }

        Object loadBalanceInstance = LoadBalanceFactory.getLoadBalance(name);
        if (loadBalanceInstance == null) {
            LOGGER.warn("get LoadBalance fail, use dubbo LoadBalance instead");
            return;
        }
        Holder<Object> instanceHolder = new Holder<>();
        instanceHolder.set(loadBalanceInstance);
        cachedInstances.put(name, instanceHolder);

        LOGGER.info("save LoadBalance in cachedInstances done");
    }

    /**
     * 在LoadBalance对象获取结束之后进行拦截，打印日志查看LoadBalance对象是否正确
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        String name = (String) args[0];
        Holder holder = (Holder) result;
        if (!DUBBO_LOADBALANCES.contains(name)) {
            return;
        }
        LOGGER.info("loadbalance: {}", holder.get().getClass());
    }
}
