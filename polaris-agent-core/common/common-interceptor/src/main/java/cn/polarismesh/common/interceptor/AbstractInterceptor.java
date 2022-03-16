package cn.polarismesh.common.interceptor;

/**
 * interceptor interface for all sort of agent operations
 */
public interface AbstractInterceptor {

    void before(Object target, Object[] args);

    void after(Object target, Object[] args, Object result, Throwable throwable);
}
