package cn.polarismesh.agent.plugin.dubbox.interceptor;

import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisRouter;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.directory.AbstractDirectory;

/**
 * interceptor for com.alibaba.dubbo.rpc.cluster.directory.AbstractDirectory#AbstractDirectory(com.alibaba.dubbo.common.URL,
 * com.alibaba.dubbo.common.URL, java.util.List)
 */
public class DubboRouterInterceptor implements AbstractInterceptor {

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        URL url = (URL) args[0];
        PolarisRouter polarisRouter = new PolarisRouter(url);
        AbstractDirectory<?> directory = (AbstractDirectory<?>) target;
        directory.getRouters().add(polarisRouter);
    }
}
