package cn.polarismesh.common.polaris;

public interface PolarisReflectConst {

    String CLAZZ_FACADE = "com.tencent.polaris.factory.api.APIFacade";

    String METHOD_INIT = "initByConfigText";

    String METHOD_REGISTER = "register";

    String METHOD_DEREGISTER = "deregister";

    String METHOD_HEARTBEAT = "heartbeat";

    String METHOD_UPDATE_SERVICE_CALL_RESULT = "updateServiceCallResult";

    String METHOD_GET_INSTANCES = "getInstances";

    String PLACE_HOLDER_ADDRESS = "${POLARIS_ADDRESS}";

    String CONFIG_TEMPLATE =
            "global:\n"
                    + "  serverConnector:\n"
                    + "    addresses:\n"
                    + "      - ${POLARIS_ADDRESS}";

    String CLAZZ_INSTANCE_PARSER = "com.tencent.polaris.factory.api.APIFacade.InstanceParser";

    String METHOD_GET_HOST = "getHost";

    String METHOD_GET_PORT = "getPort";

    String METHOD_GET_PROTOCOL = "getProtocol";

    String METHOD_GET_METADATA = "getMetadata";

    String METHOD_DESTROY = "destroy";
}
