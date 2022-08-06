package cn.polarismesh.agent.core.nacos.v1.handler;

/**
 * 针对各个接口的拦截处理类
 *
 * @author bruceppeng
 */
public interface AbstractHandler {

    String getName();

    Object handle(Object target, Object[] args, Object result)  throws Exception;

}