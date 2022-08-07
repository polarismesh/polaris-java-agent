package cn.polarismesh.agent.core.nacos.v1.handler;

import cn.polarismesh.agent.core.nacos.v1.constants.NacosConstants;

/**
 * 针对上报心跳的拦截处理类
 *
 * @author bruceppeng
 */
public class NacosSendBeatHandler extends AbstractHandler {

    @Override
    public String getName() {
        return NacosConstants.SEND_BEAT;
    }
}