package cn.polarismesh.agent.core.nacos.v1.handler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler 管理类
 *
 * @author bruceppeng
 * @date 2022/8/6 14:14
 */
public class HandlerManager {

    private static final Map<String, AbstractHandler> nacosHandlerCache = new ConcurrentHashMap<>(256);

    public HandlerManager() {
        init();
    }

    public void init(){
        NacosRegisterServiceHandler nacosRegisterServiceHandler = new NacosRegisterServiceHandler();
        NacosDeregisterServiceHandler nacosDeregisterServiceHandler = new NacosDeregisterServiceHandler();
        NacosSendBeatHandler nacosSendBeatHandler = new NacosSendBeatHandler();
        NacosQueryListHandler nacosQueryListHandler = new NacosQueryListHandler();

        nacosHandlerCache.put(nacosRegisterServiceHandler.getName(), nacosRegisterServiceHandler);
        nacosHandlerCache.put(nacosDeregisterServiceHandler.getName(), nacosDeregisterServiceHandler);
        nacosHandlerCache.put(nacosSendBeatHandler.getName(), nacosSendBeatHandler);
        nacosHandlerCache.put(nacosQueryListHandler.getName(), nacosQueryListHandler);
    }

    public Object handler(String handlerName, Object target, Object[] args, Object result) throws Exception {
        AbstractHandler handler = nacosHandlerCache.get(handlerName);
        Objects.requireNonNull(handler);
        return handler.handle(target, args, result);
    }
}