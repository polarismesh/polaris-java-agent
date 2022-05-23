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

package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.plugin.dubbo2.polaris.PolarisRegistryFactory;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.integration.InterfaceCompatibleRegistryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interceptor for org.apache.dubbo.registry.integration.RegistryProtocol#setRegistryFactory(org.apache.dubbo.registry.RegistryFactory)
 */
public class DubboRegistryInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboRegistryInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
    }

    /**
     * RegistryProtocol 的 setRegistryFactory方法
     * 替换registryFactory为PolarisRegistryFactory
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        LOGGER.debug("[POLARIS] set {}.registryFactory as PolarisRegistryFactory", target.getClass());
        if(target instanceof InterfaceCompatibleRegistryProtocol){
            ReflectionUtils.setSuperValueByFieldName(target, "registryFactory", new PolarisRegistryFactory((RegistryFactory) args[0]));
        }else{
            ReflectionUtils.setValueByFieldName(target, "registryFactory", new PolarisRegistryFactory((RegistryFactory) args[0]));
        }
    }
}