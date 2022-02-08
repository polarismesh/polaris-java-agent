package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.plugin.dubbo2.polaris.PolarisDirectory;
import cn.polarismesh.agent.plugin.dubbo2.utils.ReflectUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.RegistryService;
import org.apache.dubbo.registry.integration.RegistryDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务发现拦截器1：用于覆盖AbstractClusterInvoker.directory属性，重写list方法
 */
public class DubboClusterInvokerInterceptor implements AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboClusterInvokerInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
    }

    /**
     * 在AbstractClusterInvoker的构造器执行之后进行拦截，修改this.directory为自定义Directory对象
     * 拦截方法：public AbstractClusterInvoker(Directory<T> directory, URL url)
     *
     * @param target    拦截方法所属的对象
     * @param args      拦截方法的入参  args[0] : Directory对象  args[1] : URL对象
     * @param result    拦截方法的返回值  构造器的result为null
     * @param throwable 拦截方法抛出的异常，无异常则为null
     */
    @SuppressWarnings("unchecked")
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!(args[0] instanceof RegistryDirectory)) {
            return;
        }
        RegistryDirectory directory = (RegistryDirectory) args[0];
        URL url = (URL) args[1];
        PolarisDirectory newDirectory = new PolarisDirectory<>(RegistryService.class, url);
        newDirectory.setRouterChain(directory.getRouterChain());
        ReflectUtil.setSuperValueByFieldName(target, "directory", newDirectory);
        LOGGER.info("change directory to PolarisDirectory done");
    }
}
