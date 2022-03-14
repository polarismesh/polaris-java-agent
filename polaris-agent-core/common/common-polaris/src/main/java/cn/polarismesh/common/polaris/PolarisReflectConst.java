package cn.polarismesh.common.polaris;

import java.io.File;

public interface PolarisReflectConst {

    String POLARIS_LIB_DIR = "polaris" + File.separator + "lib";

    String POLARIS_CONF_DIR = "polaris" + File.separator + "conf";

    String POLARIS_CONF_FILE = "polaris.yml";

    String CLAZZ_FACADE = "com.tencent.polaris.factory.api.APIFacade";

    String METHOD_INIT = "initByConfiguration";

    String METHOD_REGISTER = "register";

    String METHOD_DEREGISTER = "deregister";

    String METHOD_HEARTBEAT = "heartbeat";

    String METHOD_UPDATE_SERVICE_CALL_RESULT = "updateServiceCallResult";

    String METHOD_GET_INSTANCES = "getInstances";

    String METHOD_GET_ALL_INSTANCES = "getAllInstances";

    String METHOD_WATCH_SERVICE = "watchService";

    String METHOD_UNWATCH_SERVICE = "unWatchService";

    String METHOD_GET_QUOTA = "getQuota";

    String CLAZZ_INSTANCE_PARSER = "com.tencent.polaris.factory.api.APIFacade.InstanceParser";

    String METHOD_GET_HOST = "getHost";

    String METHOD_GET_PORT = "getPort";

    String METHOD_GET_PROTOCOL = "getProtocol";

    String METHOD_GET_METADATA = "getMetadata";

    String METHOD_GET_WEIGHT = "getWeight";

    String METHOD_DESTROY = "destroy";

    String CLAZZ_CONFIG_FACTORY = "com.tencent.polaris.factory.ConfigAPIFactory";

    String METHOD_LOAD_CONFIG = "loadConfig";

    String CLAZZ_CONFIG_MODIFIER = "com.tencent.polaris.factory.api.APIFacade.ConfigurationModifier";

    String METHOD_SET_ADDRESSES = "setAddresses";

}
