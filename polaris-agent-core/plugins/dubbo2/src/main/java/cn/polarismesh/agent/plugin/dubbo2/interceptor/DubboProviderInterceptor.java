package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.plugin.dubbo2.utils.PolarisUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务注册拦截器
 */
public class DubboProviderInterceptor implements AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboProviderInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
    }

    /**
     * 在Dubbo注册至zookeeper之后向Polaris进行注册
     * 拦截方法：public <T> Exporter<T> export(final Invoker<T> invoker) throws RpcException
     *
     * @param target    拦截方法所属的对象
     * @param args      拦截方法的入参  args[0] : Invoker对象
     * @param result    拦截方法的返回值  Exporter对象
     * @param throwable 拦截方法抛出的异常，无异常则为null
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (args == null || args[0] == null || !(args[0] instanceof Invoker)) {
            LOGGER.error("Invoker object is null");
            return;
        }
        Invoker invoker = (Invoker) args[0];
        URL url = invoker.getUrl();
        if (url == null) {
            LOGGER.error("URL object is null");
            return;
        }
        PolarisUtil.register(url);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> PolarisUtil.shutdown(url)));
    }
}