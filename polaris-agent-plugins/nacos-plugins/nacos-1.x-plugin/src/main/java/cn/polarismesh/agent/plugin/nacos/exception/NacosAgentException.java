package cn.polarismesh.agent.plugin.nacos.exception;

import cn.polarismesh.agent.core.common.exception.PolarisAgentException;

public class NacosAgentException extends PolarisAgentException {
    public NacosAgentException() {
    }

    public NacosAgentException(String message) {
        super(message);
    }

    public NacosAgentException(String message, Throwable cause) {
        super(message, cause);
    }

    public NacosAgentException(Throwable cause) {
        super(cause);
    }
}
