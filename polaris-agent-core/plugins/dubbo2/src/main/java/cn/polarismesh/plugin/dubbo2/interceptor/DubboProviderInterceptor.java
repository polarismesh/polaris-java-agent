package cn.polarismesh.plugin.dubbo2.interceptor;

import cn.polarismesh.plugin.dubbo2.utils.PolarisUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invoker;

public class DubboProviderInterceptor implements Interceptor {
    @Override
    public void before(Object target, Object[] args) {
        if (args == null || args[0] == null || !(args[0] instanceof Invoker)) {
            System.out.println("[ERROR] Invoker object is null");
            return;
        }
        Invoker invoker = (Invoker) args[0];
        URL url = invoker.getUrl();
        if (url == null) {
            System.out.println("[ERROR] URL object is null");
            return;
        }
        PolarisUtil.register(url);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> PolarisUtil.deregister(url)));
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}