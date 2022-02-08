package cn.polarismesh.agent.adapter.spring.cloud.interceptor;

import cn.polarismesh.agent.core.spring.cloud.BeforePolarisInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

/**
 * Polaris服务属性获取拦截器
 *
 * @author zhuyuhan
 */
public class PolarisAgentPropertiesInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {
        BeforePolarisInterceptor beforePolarisInterceptor = new cn.polarismesh.agent.core.spring.cloud.support.PolarisAgentPropertiesInterceptor();
        beforePolarisInterceptor.beforeInterceptor(target, args, null);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }

}

