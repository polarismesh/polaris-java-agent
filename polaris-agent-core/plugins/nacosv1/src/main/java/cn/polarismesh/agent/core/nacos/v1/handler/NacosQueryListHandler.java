package cn.polarismesh.agent.core.nacos.v1.handler;

import cn.polarismesh.agent.core.nacos.v1.constants.NacosConstants;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * 针对查询服务实例的拦截处理类
 *
 * @author bruceppeng
 */
public class NacosQueryListHandler extends AbstractHandler {

    @Override
    public String getName() {
        return NacosConstants.QUERY_LIST;
    }

}