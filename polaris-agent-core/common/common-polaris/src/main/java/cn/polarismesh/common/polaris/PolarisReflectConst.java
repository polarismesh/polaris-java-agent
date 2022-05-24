/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
