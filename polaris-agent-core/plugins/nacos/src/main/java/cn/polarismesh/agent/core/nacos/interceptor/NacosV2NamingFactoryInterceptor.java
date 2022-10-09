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

package cn.polarismesh.agent.core.nacos.interceptor;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.core.nacos.adapter.NamingClientProxyAdapter;
import cn.polarismesh.agent.core.nacos.constants.NacosConstants;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.event.InstancesChangeNotifier;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * nacos sdk2.1.0版本NamingService拦截类
 *
 * @author bruceppeng
 */
public class NacosV2NamingFactoryInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosV2NamingFactoryInterceptor.class);


    public NacosV2NamingFactoryInterceptor() {

    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        NacosNamingService nacosNamingService = (NacosNamingService)result;

        //构造 NamingClientProxyAdapter 对象
        Properties properties = (Properties)args[0];
        String namespace = (String)ReflectionUtils.getObjectByFieldName(nacosNamingService, NacosConstants.NAMESPACE);
        ServiceInfoHolder serviceInfoHolder = (ServiceInfoHolder)ReflectionUtils.getObjectByFieldName(nacosNamingService, NacosConstants.SERVICE_INFO_HOLDER);
        InstancesChangeNotifier changeNotifier = (InstancesChangeNotifier)ReflectionUtils.getObjectByFieldName(nacosNamingService, NacosConstants.CHANGE_NOTIFIER);

        NamingClientProxyAdapter clientProxy = null;
        try {
            clientProxy = new NamingClientProxyAdapter(namespace, serviceInfoHolder, properties, changeNotifier);
        } catch (NacosException e) {
            LOGGER.error("[Nacos] fail to create NamingClientProxyAdapter",e);
        }

        //给nacosNamingService对象重新设置属性HostReactor对象
        ReflectionUtils.setValueByFieldName(nacosNamingService, NacosConstants.CLIENT_PROXY, clientProxy);
    }
}
