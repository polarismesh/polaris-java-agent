package cn.polarismesh.agent.plugin.dubbox.interceptor;

import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisLoadBalance;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;

/**
 * interceptor for com.alibaba.dubbo.common.extension.ExtensionLoader#createExtension(java.lang.String)
 */
public class DubboLoadBalanceInterceptor implements AbstractInterceptor {

    @Override
    public void before(Object target, Object[] args) {
        ExtensionLoader<?> extensionLoader = (ExtensionLoader<?>) target;
        if (!Constants.DEFAULT_LOADBALANCE.equals(args[0])) {
            return;
        }
        extensionLoader.replaceExtension(Constants.DEFAULT_LOADBALANCE, PolarisLoadBalance.class);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}