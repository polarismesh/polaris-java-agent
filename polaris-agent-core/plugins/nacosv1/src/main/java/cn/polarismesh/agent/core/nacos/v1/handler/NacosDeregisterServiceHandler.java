package cn.polarismesh.agent.core.nacos.v1.handler;

import cn.polarismesh.agent.core.nacos.v1.constants.NacosConstants;

/**
 * 针对各个接口的拦截处理类
 *
 * @author bruceppeng
 */
public class NacosDeregisterServiceHandler implements AbstractHandler {

    @Override
    public String getName() {
        return NacosConstants.DEREGISTER_SERVICE;
    }

    @Override
    public Object handle(Object target, Object[] args, Object result) throws Exception {
        return null;
    }
}