package cn.polarismesh.agent.core.nacos.v1.handler;

import cn.polarismesh.agent.core.nacos.v1.interceptor.NacosV1NamingProxyInterceptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler 管理类
 *
 * @author bruceppeng
 */
public class HandlerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerManager.class);
    private static final Map<String, AbstractHandler> nacosHandlerCache = new ConcurrentHashMap<>(256);

    public HandlerManager() {
        init();
    }

    public void init(){
        NacosRegisterServiceHandler nacosRegisterServiceHandler = new NacosRegisterServiceHandler();
        NacosDeregisterServiceHandler nacosDeregisterServiceHandler = new NacosDeregisterServiceHandler();
        NacosSendBeatHandler nacosSendBeatHandler = new NacosSendBeatHandler();

        nacosHandlerCache.put(nacosRegisterServiceHandler.getName(), nacosRegisterServiceHandler);
        nacosHandlerCache.put(nacosDeregisterServiceHandler.getName(), nacosDeregisterServiceHandler);
        nacosHandlerCache.put(nacosSendBeatHandler.getName(), nacosSendBeatHandler);
    }

    public void handler(String handlerName, Object target, Object[] args, Object result){
        AbstractHandler handler = nacosHandlerCache.get(handlerName);
//        Objects.requireNonNull(handler);
        if (Objects.isNull(handler)){
            LOGGER.warn("HandlerManager handler not exist, handlerName:{}", handlerName);
            return;
        }
        handler.handle(target, args, result);
    }
}