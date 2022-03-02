package cn.polarismesh.common.polaris;

import java.io.File;

public interface PolarisReflectConst {

    String POLARIS_LIB_DIR = "polaris" + File.separator + "lib";

    String CLAZZ_FACADE = "com.tencent.polaris.factory.api.APIFacade";

    String METHOD_INIT = "initByConfigText";

    String METHOD_REGISTER = "register";

    String METHOD_DEREGISTER = "deregister";

    String METHOD_HEARTBEAT = "heartbeat";

    String METHOD_UPDATE_SERVICE_CALL_RESULT = "updateServiceCallResult";

    String METHOD_GET_INSTANCES = "getInstances";

    String CLAZZ_INSTANCE_PARSER = "com.tencent.polaris.factory.api.APIFacade.InstanceParser";

    String METHOD_GET_HOST = "getHost";

    String METHOD_GET_PORT = "getPort";

    String METHOD_GET_PROTOCOL = "getProtocol";

    String METHOD_GET_METADATA = "getMetadata";

    String METHOD_GET_WEIGHT = "getWeight";

    String METHOD_DESTROY = "destroy";

    String TEMPLATE_PATH = "conf/polaris-template.yaml";

    String PLACE_HOLDER_ADDRESS = "${POLARIS_ADDRESS}";

    String PLACE_REFRESH_INTERVAL = "${REFRESH_INTERVAL}";
}
