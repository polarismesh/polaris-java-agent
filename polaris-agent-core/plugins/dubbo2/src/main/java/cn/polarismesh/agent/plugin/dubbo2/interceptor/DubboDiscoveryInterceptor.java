package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.plugin.dubbo2.entity.InstanceInvoker;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.apache.dubbo.rpc.Invoker;

import java.util.Map;

/**
 * interceptor for com.alibaba.dubbo.registry.integration.RegistryDirectory#toInvokers(java.util.List)
 */
public class DubboDiscoveryInterceptor implements AbstractInterceptor {

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Map<String, Invoker<?>> invokers = (Map<String, Invoker<?>>) result;
        if (null == invokers || invokers.size() == 0) {
            return;
        }
        for (Map.Entry<String, Invoker<?>> entry : invokers.entrySet()) {
            invokers.put(entry.getKey(), new InstanceInvoker<>(entry.getValue()));
        }
    }
}
