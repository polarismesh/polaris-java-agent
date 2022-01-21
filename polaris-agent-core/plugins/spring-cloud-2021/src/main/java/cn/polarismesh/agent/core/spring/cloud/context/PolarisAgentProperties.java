package cn.polarismesh.agent.core.spring.cloud.context;

/**
 * Polaris服务属性实体类
 *
 * @author zhuyuhan
 */
public class PolarisAgentProperties {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 服务名
     */
    private String service;

    /**
     * 主机
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * token
     */
    private String serverToken;

    /**
     * Polaris 地址
     */
    private String serverAddress;

    /**
     * 协议
     */
    private String protocol;

    public PolarisAgentProperties() {
    }

    public PolarisAgentProperties(String namespace, String service, String host, Integer port, String serverToken, String serverAddress, String protocol) {
        this.namespace = namespace;
        this.service = service;
        this.host = host;
        this.port = port;
        this.serverToken = serverToken;
        this.serverAddress = serverAddress;
        this.protocol = protocol;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getServerToken() {
        return serverToken;
    }

    public void setServerToken(String serverToken) {
        this.serverToken = serverToken;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "PolarisAgentProperties{" +
                "namespace='" + namespace + '\'' +
                ", service='" + service + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", serverToken='" + serverToken + '\'' +
                ", serverAddress='" + serverAddress + '\'' +
                ", protocol='" + protocol + '\'' +
                '}';
    }
}
