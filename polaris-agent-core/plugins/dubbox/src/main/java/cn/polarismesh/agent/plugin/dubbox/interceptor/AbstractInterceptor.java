package cn.polarismesh.agent.plugin.dubbox.interceptor;

public interface AbstractInterceptor {
    void before(Object target, Object[] args);

    void after(Object target, Object[] args, Object result, Throwable throwable);
}
