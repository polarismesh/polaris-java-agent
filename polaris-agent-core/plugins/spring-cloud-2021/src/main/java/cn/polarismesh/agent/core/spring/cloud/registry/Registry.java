package cn.polarismesh.agent.core.spring.cloud.registry;

/**
 * 服务注册接口
 */
public interface Registry {

    void register();

    void deregister();
}
