package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.plugin.dubbo2.polaris.PolarisLoadBalance;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.ExtensionLoader;

/**
 * interceptor for org.apache.dubbo.common.extension.ExtensionLoader#createExtension(java.lang.String, boolean)
 */
public class DubboLoadBalanceInterceptor implements AbstractInterceptor {

    @Override
    public void before(Object target, Object[] args) {
        ExtensionLoader<?> extensionLoader = (ExtensionLoader<?>) target;
        if (!CommonConstants.DEFAULT_LOADBALANCE.equals(args[0])) {
            return;
        }
        extensionLoader.replaceExtension(CommonConstants.DEFAULT_LOADBALANCE, PolarisLoadBalance.class);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}