package cn.polarismesh.common.polaris;

import java.util.Objects;

public class InstanceIdentifier {

    private final String service;

    private final String host;

    private final int port;

    public InstanceIdentifier(String service, String host, int port) {
        this.service = service;
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InstanceIdentifier)) {
            return false;
        }
        InstanceIdentifier that = (InstanceIdentifier) o;
        return port == that.port &&
                Objects.equals(service, that.service) &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, host, port);
    }
}
