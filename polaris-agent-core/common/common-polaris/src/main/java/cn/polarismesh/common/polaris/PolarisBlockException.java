package cn.polarismesh.common.polaris;

import java.util.Map;

public class PolarisBlockException extends RuntimeException {

    private final String namespace;
    private final String service;
    private final String method;
    private final Map<String, String> metadata;

    public PolarisBlockException(String message) {
        this(message, null, null, null, null);
    }

    public PolarisBlockException(String message, String namespace, String service, String method, Map<String, String> metadata) {
        super(message);
        this.namespace = namespace;
        this.service = service;
        this.method = method;
        this.metadata = metadata;
    }

    @Override
    public String getMessage() {
        return super.getMessage() +
                ", namespace: " + namespace +
                ", service: " + service +
                ", method: " + method +
                ", metadata: " + metadata;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
