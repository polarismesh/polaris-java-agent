package cn.polarismesh.agent.core.spring.cloud.context;


import com.tencent.polaris.api.utils.StringUtils;

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

    private PolarisAgentProperties() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        /**
         * 命名空间
         */
        private String namespace = "default";

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
        private String port = "8080";

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
        private String protocol = "grpc";

        public Builder withNamespace(String namespace) {
            if (!StringUtils.isEmpty(namespace)) {
                this.namespace = namespace;
            }
            return this;
        }

        public Builder withService(String service) {
            this.service = service;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(String port) {
            if (!StringUtils.isEmpty(port)) {
                this.port = port;
            }
            return this;
        }

        public Builder withServerToken(String serverToken) {
            this.serverToken = serverToken;
            return this;
        }

        public Builder withServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        public Builder withProtocol(String protocol) {
            if (!StringUtils.isEmpty(protocol)) {
                this.protocol = protocol;
            }
            return this;
        }

        public PolarisAgentProperties build() {
            PolarisAgentProperties polarisAgentProperties = new PolarisAgentProperties();
            polarisAgentProperties.setHost(this.host);
            polarisAgentProperties.setPort(Integer.valueOf(this.port));
            polarisAgentProperties.setNamespace(this.namespace);
            polarisAgentProperties.setService(this.service);
            polarisAgentProperties.setProtocol(this.protocol);
            polarisAgentProperties.setServerAddress(this.serverAddress);
            polarisAgentProperties.setServerToken(this.serverToken);
            return polarisAgentProperties;
        }
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
