package cn.polarismesh.agent.core.nacos.v1.handler;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.core.nacos.v1.constants.NacosConstants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.naming.net.NamingProxy;
import java.util.Map;
import java.util.Objects;

/**
 * 针对各个接口的拦截处理类
 *
 * @author bruceppeng
 */
public abstract class AbstractHandler {

    abstract String getName();

    private int maxRetry;

    void handle(Object target, Object[] args, Object result){

        //1.获取代理类对象
        NamingProxy namingProxy = (NamingProxy) target;

        //2.获取内置重试次数
        if (maxRetry <= 0)
            maxRetry = (int)ReflectionUtils.getObjectByFieldName(namingProxy, NacosConstants.MAX_RETRY);

        //3.提取调用方法参数
        String api = (String)args[0];
        Map<String, String> params = (Map<String, String>)args[1];
        Map<String, String> body = (Map<String, String>)args[2];
        String method = (String)args[4];
        String nacosDomain = System.getProperty(NacosConstants.TSE_NACOS_SERVER_ADDR);
        Objects.requireNonNull(nacosDomain);

        //4.执行调用
        for (int i = 0; i < maxRetry; i++) {
            try {
                //1.请求另外一个nacos server
                Object secondResult = namingProxy.callServer(api, params, body, nacosDomain, method);
                //2.merge两次调用的结果
                mergeResult(result, secondResult);
            } catch (NacosException e) {
                if (NAMING_LOGGER.isDebugEnabled()) {
                    NAMING_LOGGER.debug("request {} failed.", nacosDomain, e);
                }
            }
        }
    }

    void mergeResult(Object result, Object secondResult){

    }

}