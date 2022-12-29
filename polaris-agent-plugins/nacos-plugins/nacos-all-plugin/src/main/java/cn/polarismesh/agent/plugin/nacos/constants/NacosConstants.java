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

package cn.polarismesh.agent.plugin.nacos.constants;

import cn.polarismesh.agent.plugin.nacos.adapter.UtilAndComsAdapter;


public class NacosConstants {


    //other nacos server address
    public static final String OTHER_NACOS_SERVER_ADDR = "other.nacos.server.addr";
    //主 Nacos 集群名称
    public static final String NACOS_CLUSTER_NAME = "nacos.cluster.name";
    //就近路由级别（null, nacos-cluster），默认为null
    public static final String ROUTER_NEARBY_LEVEL = "router.nearby.level";
    //元数据标示
    public static final String METADATA = "metadata";

    //反射所有属性名称
    public static final String CACHE_DIR = "cacheDir";
    public static final String NAMESPACE = "namespace";
    public static final String ENDPOINT = "endpoint";
    public static final String SERVER_LIST = "serverList";

    public static final String SERVER_PROXY = "serverProxy";
    public static final String BEAT_REACTOR = "beatReactor";
    public static final String HOST_REACTOR = "hostReactor";

    public static final String CLIENT_PROXY = "clientProxy";
    public static final String SERVICE_INFO_HOLDER = "serviceInfoHolder";
    public static final String CHANGE_NOTIFIER = "changeNotifier";

    //反射所用方法名称
    public static final String METHDO_INIT_CLIENT_BEAT_THREAD_COUNT = "initClientBeatThreadCount";
    public static final String METHDO_IS_LOAD_CACHE_AT_START = "isLoadCacheAtStart";
    public static final String METHDO_INIT_POLLING_THREAD_COUNT = "initPollingThreadCount";
    public static final String METHDO_IS_PUSH_EMPTY_PROTECT = "isPushEmptyProtect";

    // http method
    public static final String GET = "GET";
    public static final String GET_LARGE = "GET-LARGE";
    public static final String HEAD = "HEAD";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";
    public static final String DELETE = "DELETE";
    public static final String DELETE_LARGE = "DELETE_LARGE";
    public static final String OPTIONS = "OPTIONS";
    public static final String TRACE = "TRACE";

    public static final String LINK_FLAG = "@";

    public static final String REGISTER_SERVICE = UtilAndComsAdapter.NACOS_URL_INSTANCE + LINK_FLAG + POST;

    public static final String DEREGISTER_SERVICE = UtilAndComsAdapter.NACOS_URL_INSTANCE + LINK_FLAG + DELETE;

    public static final String SEND_BEAT = UtilAndComsAdapter.NACOS_URL_BASE + "/instance/beat" + LINK_FLAG + PUT;

    public static final String QUERY_LIST = UtilAndComsAdapter.NACOS_URL_BASE + "/instance/list" + LINK_FLAG + GET;

    //    public static final String NAMING_PROXY = "com.alibaba.nacos.client.naming.net.NamingProxy";
    public static final String NAMING_PROXY = "cn.polarismesh.agent.plugin.nacos.delegate.DynamicNamingProxy";
    public static final String NAMING_CLIENT_PROXY = "cn.polarismesh.agent.plugin.nacos.delegate.DynamicNamingClientProxy";
    public static final String NAMING_PROXY_PATH = "cn/polarismesh/agent/plugin/nacos/delegate/NamingProxy";
    public static final String TARGET_METHOD = "callServer";
    public static final String STRING_TYPE = "java.lang.String";
    public static final String MAP_TYPE = "java.util.Map";


    public static final String NACOS_NAMING_FACTORY = "com.alibaba.nacos.api.naming.NamingFactory";


    public static final String NAMING_REQUEST_DOMAIN_RETRY_COUNT = "namingRequestDomainMaxRetryCount";

}
