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

import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.common.utils.HttpMethod;

/**
 * Nacos 相关常量
 *
 * @author bruceppeng
 */
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
    public static final String CLIENT_PROXY = "clientProxy";
    public static final String NAMESPACE = "namespace";
    public static final String SERVICE_INFO_HOLDER = "serviceInfoHolder";
    public static final String CHANGE_NOTIFIER = "changeNotifier";

}
