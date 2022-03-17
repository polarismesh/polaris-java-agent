package cn.polarismesh.agent.plugin.dubbox.interceptor;

import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisFilterWrapper;
import cn.polarismesh.agent.plugin.dubbox.utils.ReflectUtil;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * interceptor for com.alibaba.dubbo.rpc.protocol.AbstractExporter#AbstractExporter(com.alibaba.dubbo.rpc.Invoker)
 */
public class DubboRateLimitInterceptor implements AbstractInterceptor {

    //private static final Logger LOGGER = LoggerFactory.getLogger(DubboRateLimitInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
    }

    /**
     * 拦截com.alibaba.dubbo.rpc.protocol.AbstractExporter的构造器
     * 替换其invoker对象为自定义的invoker对象，用于接入限流功能
     */
    @SuppressWarnings("unchecked")
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        //LOGGER.info("[POLARIS] set {}.invoker filter with rate limit", target.getClass());
        Invoker invoker = PolarisFilterWrapper.buildInvokerChain((Invoker) args[0]);
        ReflectUtil.setSuperValueByFieldName(target, "invoker", invoker);
    }
}