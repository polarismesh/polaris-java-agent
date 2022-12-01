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

package cn.polarismesh.agent.plugin.nacos.interceptor;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;
import cn.polarismesh.agent.plugin.nacos.delegate.NacosNamingProxy;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.client.naming.beat.BeatReactor;
import com.alibaba.nacos.client.naming.core.HostReactor;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * nacos sdk1.4.1版本NamingService拦截类
 *
 * @author bruceppeng
 */
public class NacosNamingFactoryInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosNamingFactoryInterceptor.class);


    public NacosNamingFactoryInterceptor() {

    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        NacosNamingService nacosNamingService = (NacosNamingService)result;

        //构造NacosNamingProxy对象
        Properties properties = (Properties)args[0];
        String namespace = (String)ReflectionUtils.getObjectByFieldName(nacosNamingService, NacosConstants.NAMESPACE);
        String endpoint = (String)ReflectionUtils.getObjectByFieldName(nacosNamingService, NacosConstants.ENDPOINT);
        String serverList = (String)ReflectionUtils.getObjectByFieldName(nacosNamingService, NacosConstants.SERVER_LIST);
        NacosNamingProxy nacosNamingProxy = new NacosNamingProxy(namespace, endpoint, serverList, properties);

        //构造BeatReactor对象
        int threadCount = (int)ReflectionUtils.invokeMethodByName(nacosNamingService, NacosConstants.METHDO_INIT_CLIENT_BEAT_THREAD_COUNT, properties);
        BeatReactor beatReactor = new BeatReactor(nacosNamingProxy, threadCount);

        //构造HostReactor对象
        boolean loadCacheAtStart = (boolean)ReflectionUtils.invokeMethodByName(nacosNamingService, NacosConstants.METHDO_IS_LOAD_CACHE_AT_START, properties);
        boolean pushEmptyProtection = (boolean)ReflectionUtils.invokeMethodByName(nacosNamingService, NacosConstants.METHDO_IS_PUSH_EMPTY_PROTECT, properties);
        int pollingThreadCount = (int)ReflectionUtils.invokeMethodByName(nacosNamingService, NacosConstants.METHDO_INIT_POLLING_THREAD_COUNT, properties);
        String cacheDir = (String)ReflectionUtils.getObjectByFieldName(nacosNamingService, NacosConstants.CACHE_DIR);
        HostReactor hostReactor = new HostReactor(nacosNamingProxy, beatReactor, cacheDir, loadCacheAtStart, pushEmptyProtection, pollingThreadCount);

        //给nacosNamingService对象重新设置属性NamingProxy对象
        ReflectionUtils.setValueByFieldName(nacosNamingService, NacosConstants.SERVER_PROXY, nacosNamingProxy);

        //给nacosNamingService对象重新设置属性BeatReactor对象
        ReflectionUtils.setValueByFieldName(nacosNamingService, NacosConstants.BEAT_REACTOR, beatReactor);

        //给nacosNamingService对象重新设置属性HostReactor对象
        ReflectionUtils.setValueByFieldName(nacosNamingService, NacosConstants.HOST_REACTOR, hostReactor);
    }
}
