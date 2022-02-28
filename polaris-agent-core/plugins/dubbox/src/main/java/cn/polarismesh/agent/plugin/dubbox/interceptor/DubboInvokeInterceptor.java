package cn.polarismesh.agent.plugin.dubbox.interceptor;

import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisSingleton;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;

/**
 * 统计时延信息、上报服务调用结果
 */
public class DubboInvokeInterceptor implements AbstractInterceptor {

    private final ThreadLocal<Long> startTimeMilli = new ThreadLocal<>();

    @Override
    public void before(Object target, Object[] args) {
        this.startTimeMilli.set(System.currentTimeMillis());
    }

    /**
     * 在AbstractClusterInvoker的invoke()方法之后进行拦截，统计本次服务调用时延数据，上报polaris
     * public Result invoke(final Invocation invocation) throws RpcException
     *
     * @param target 拦截方法所属的对象
     * @param args 拦截方法的入参  args[0] : Invocation对象
     * @param result 拦截方法的返回值 Result对象
     * @param throwable 拦截方法抛出的异常，无异常则为null
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        long delay = System.currentTimeMillis() - this.startTimeMilli.get();
        Invocation invocation = (Invocation) args[0];
        URL url = invocation.getInvoker().getUrl();
        PolarisSingleton.getPolarisOperation().reportInvokeResult(
                url.getServiceInterface(), url.getHost(), url.getPort(), delay, null == throwable,
                null != throwable ? -1 : 0);
    }
}
