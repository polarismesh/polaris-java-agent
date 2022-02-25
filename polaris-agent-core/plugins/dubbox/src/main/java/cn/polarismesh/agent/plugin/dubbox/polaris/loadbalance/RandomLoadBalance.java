package cn.polarismesh.agent.plugin.dubbox.polaris.loadbalance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡策略
 */
public class RandomLoadBalance extends PolarisAbstractLoadBalance {

    /**
     * 随机负载均衡实现
     *
     * @param invokers 待选择的Invoker对象列表
     * @param url 本次服务调用的URL对象
     * @param invocation 本次服务调用的Invocation对象
     * @return 根据负载均衡策略选取的Invoker对象
     */
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        int idx = ThreadLocalRandom.current().nextInt(length);
        return invokers.get(idx);
    }
}
