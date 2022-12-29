package cn.polarismesh.agent.plugin.nacos.exception;

public class UnsupportedNacosClientException extends NacosAgentException {

    public UnsupportedNacosClientException() {
        super("unsupported nacos client version");
    }

    public UnsupportedNacosClientException(String message) {
        super(message);
    }

    public UnsupportedNacosClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedNacosClientException(Throwable cause) {
        super("unsupported nacos client version", cause);
    }
}
