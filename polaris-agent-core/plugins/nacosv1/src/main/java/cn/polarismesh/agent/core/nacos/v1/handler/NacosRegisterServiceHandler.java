package cn.polarismesh.agent.core.nacos.v1.handler;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

import cn.polarismesh.agent.core.nacos.v1.constants.NacosConstants;
import com.alibaba.nacos.api.exception.NacosException;

/**
 * 针对注册的拦截处理类
 *
 * @author bruceppeng
 */
public class NacosRegisterServiceHandler extends AbstractHandler {

    @Override
    public String getName() {
        return NacosConstants.REGISTER_SERVICE;
    }

}