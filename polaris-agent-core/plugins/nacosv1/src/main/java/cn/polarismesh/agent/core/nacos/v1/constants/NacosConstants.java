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

package cn.polarismesh.agent.core.nacos.v1.constants;

import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.common.utils.HttpMethod;

/**
 * Nacos 相关常量
 *
 * @author bruceppeng
 */
public class NacosConstants {

    //tse nacos server address
    public static final String TARGET_NACOS_SERVER_ADDR = "target.nacos.server.addr";

    //反射所有属性名称
    public static final String CACHE_DIR = "cacheDir";
    public static final String NAMESPACE = "namespace";
    public static final String ENDPOINT = "endpoint";
    public static final String SERVER_LIST = "serverList";
    public static final String EVENT_DISPATCHER = "eventDispatcher";

    public static final String SERVER_PROXY = "serverProxy";
    public static final String BEAT_REACTOR = "beatReactor";
    public static final String HOST_REACTOR = "hostReactor";

    //反射所用方法名称
    public static final String METHDO_INIT_CLIENT_BEAT_THREAD_COUNT = "initClientBeatThreadCount";
    public static final String METHDO_IS_LOAD_CACHE_AT_START = "isLoadCacheAtStart";
    public static final String METHDO_INIT_POLLING_THREAD_COUNT = "initPollingThreadCount";
    public static final String METHDO_IS_PUSH_EMPTY_PROTECT = "isPushEmptyProtect";


    public static final String LINK_FLAG = "@";

    public static final String REGISTER_SERVICE = UtilAndComs.nacosUrlInstance + LINK_FLAG + HttpMethod.POST;

    public static final String DEREGISTER_SERVICE = UtilAndComs.nacosUrlInstance + LINK_FLAG + HttpMethod.DELETE;

    public static final String SEND_BEAT = UtilAndComs.nacosUrlBase + "/instance/beat" + LINK_FLAG + HttpMethod.PUT;

    public static final String QUERY_LIST = UtilAndComs.nacosUrlBase + "/instance/list" + LINK_FLAG + HttpMethod.GET;

}
