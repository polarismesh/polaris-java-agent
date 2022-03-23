package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.plugin.dubbo2.polaris.PolarisRouter;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.cluster.RouterChain;
import org.apache.dubbo.rpc.cluster.directory.AbstractDirectory;

import java.util.Collections;

/**
 * interceptor for org.apache.dubbo.rpc.cluster.directory.AbstractDirectory#setRouterChain(org.apache.dubbo.rpc.cluster.RouterChain)
 */
public class DubboRouterInterceptor implements AbstractInterceptor {

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        RouterChain<?> routerChain = (RouterChain<?>) args[0];
        if (null == routerChain) {
            return;
        }
        AbstractDirectory<?> directory = (AbstractDirectory<?>) target;
        URL url = directory.getUrl();
        PolarisRouter polarisRouter = new PolarisRouter(url);
        directory.getRouterChain().addRouters(Collections.singletonList(polarisRouter));
    }
}
