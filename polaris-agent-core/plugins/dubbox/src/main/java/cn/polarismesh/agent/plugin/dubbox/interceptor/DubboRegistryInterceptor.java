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

package cn.polarismesh.agent.plugin.dubbox.interceptor;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.plugin.dubbox.polaris.PolarisRegistryFactory;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interceptor for com.alibaba.dubbo.registry.integration.RegistryProtocol#setRegistryFactory(com.alibaba.dubbo.registry.RegistryFactory)
 */
public class DubboRegistryInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboRegistryInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {
    }

    /**
     * 替换registryFactory为PolarisRegistryFactory
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        LOGGER.debug("[POLARIS] set {}.registryFactory as PolarisRegistryFactory", target.getClass());
        ReflectionUtils.setValueByFieldName(target, "registryFactory", new PolarisRegistryFactory());
    }
}