package cn.polarismesh.agent.core.nacos.v1.handler;

import cn.polarismesh.agent.core.nacos.v1.constants.NacosConstants;

/**
 * 针对反注册的拦截处理类
 *
 * @author bruceppeng
 */
public class NacosDeregisterServiceHandler extends AbstractHandler {

    @Override
    public String getName() {
        return NacosConstants.DEREGISTER_SERVICE;
    }

}