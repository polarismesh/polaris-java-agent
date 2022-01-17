package cn.polarismesh.agent.plugin.dubbo2.interceptor;

public interface AroundInterceptor {
    void before(Object target, Object[] args);

    void after(Object target, Object[] args, Object result, Throwable throwable);
}
